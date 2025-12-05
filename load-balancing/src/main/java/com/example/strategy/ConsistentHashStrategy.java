package com.example.strategy;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class ConsistentHashStrategy implements LoadBalancerStrategy {

    private static final int VIRTUAL_NODES = 150; // Number of virtual nodes per physical worker
    private final SortedMap<Integer, Integer> hashRing = new TreeMap<>();
    private volatile int currentTotalWorkers = 0;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public int selectWorker(String key, int totalWorkers) {
        if (totalWorkers <= 0) {
            throw new IllegalArgumentException("totalWorkers must be > 0");
        }
        
        // Rebuild ring if number of workers changed (double-checked locking)
        if (currentTotalWorkers != totalWorkers) {
            lock.writeLock().lock();
            try {
                if (currentTotalWorkers != totalWorkers) {
                    buildHashRing(totalWorkers);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        int hash = hashKey(key);
        
        // Read from the ring with read lock
        lock.readLock().lock();
        try {
            // Find the first node in the ring with hash >= requested hash
            SortedMap<Integer, Integer> tailMap = hashRing.tailMap(hash);
            int nodeHash = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();
            return hashRing.get(nodeHash);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void buildHashRing(int totalWorkers) {
        // Called while holding write lock
        hashRing.clear();
        
        // Add virtual nodes for each physical worker
        for (int workerId = 1; workerId <= totalWorkers; workerId++) {
            for (int virtualNode = 0; virtualNode < VIRTUAL_NODES; virtualNode++) {
                String virtualNodeKey = "worker-" + workerId + "-vnode-" + virtualNode;
                int hash = hashKey(virtualNodeKey);
                hashRing.put(hash, workerId);
            }
        }
        
        currentTotalWorkers = totalWorkers;
    }

    private int hashKey(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            
            // Convert first 4 bytes to int
            int hash = 0;
            for (int i = 0; i < 4; i++) {
                hash = (hash << 8) | (digest[i] & 0xFF);
            }
            
            return hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    @Override
    public String getName() {
        return "consistent-hash";
    }

    /**
     * Reset the hash ring (useful for testing)
     */
    public void reset() {
        lock.writeLock().lock();
        try {
            hashRing.clear();
            currentTotalWorkers = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }
}

