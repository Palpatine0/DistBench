package com.example.strategy;

import com.example.config.CacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LRU with TTL Cache Strategy
 * Implements Least Recently Used eviction with Time-To-Live expiration.
 * 
 * Features:
 * - LRU eviction when cache is full
 * - TTL-based expiration for entries
 * - Optional TTL reset on access (configurable)
 * - Thread-safe operations
 */
@Component
public class LRUWithTTLStrategy implements CacheStrategy {

    @Autowired
    private CacheConfig cacheConfig;

    // Cache entry with value and expiration time
    private static class CacheEntry {
        final String value;
        long expirationTime;

        CacheEntry(String value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    // LRU cache using LinkedHashMap with access-order
    private LinkedHashMap<String, CacheEntry> cache;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // Statistics
    private final AtomicInteger hits = new AtomicInteger(0);
    private final AtomicInteger misses = new AtomicInteger(0);
    private final AtomicInteger evictions = new AtomicInteger(0);
    private final AtomicInteger expirations = new AtomicInteger(0);

    // Configurable parameters (can be overridden per scenario)
    private int maxSize;
    private long ttlMs;
    private boolean resetTtlOnAccess;

    public LRUWithTTLStrategy() {
        // Initialize with defaults, will be overridden by config
        this.maxSize = 100;
        this.ttlMs = 5000;
        this.resetTtlOnAccess = true;
        initCache();
    }

    private void initCache() {
        // LinkedHashMap with accessOrder=true for LRU behavior
        this.cache = new LinkedHashMap<String, CacheEntry>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                if (size() > maxSize) {
                    evictions.incrementAndGet();
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public String get(String key) {
        lock.writeLock().lock();
        try {
            CacheEntry entry = cache.get(key);
            
            if (entry == null) {
                misses.incrementAndGet();
                return null;
            }
            
            if (entry.isExpired()) {
                cache.remove(key);
                expirations.incrementAndGet();
                misses.incrementAndGet();
                return null;
            }
            
            // Optionally reset TTL on access
            if (resetTtlOnAccess) {
                entry.expirationTime = System.currentTimeMillis() + ttlMs;
            }
            
            hits.incrementAndGet();
            return entry.value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void put(String key, String value) {
        lock.writeLock().lock();
        try {
            long expirationTime = System.currentTimeMillis() + ttlMs;
            cache.put(key, new CacheEntry(value, expirationTime));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void reset() {
        lock.writeLock().lock();
        try {
            cache.clear();
            hits.set(0);
            misses.set(0);
            evictions.set(0);
            expirations.set(0);
            
            // Re-read config values
            if (cacheConfig != null) {
                this.maxSize = cacheConfig.getMaxSize();
                this.ttlMs = cacheConfig.getTtlSeconds() * 1000;
                this.resetTtlOnAccess = cacheConfig.isResetTtlOnAccess();
            }
            initCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    // Configuration methods for scenarios
    public void setMaxSize(int maxSize) {
        lock.writeLock().lock();
        try {
            this.maxSize = maxSize;
            initCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setTtlMs(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    public void setResetTtlOnAccess(boolean resetTtlOnAccess) {
        this.resetTtlOnAccess = resetTtlOnAccess;
    }

    // Statistics getters
    public int getHits() {
        return hits.get();
    }

    public int getMisses() {
        return misses.get();
    }

    public int getEvictions() {
        return evictions.get();
    }

    public int getExpirations() {
        return expirations.get();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public long getTtlMs() {
        return ttlMs;
    }

    public double getHitRate() {
        int totalRequests = hits.get() + misses.get();
        if (totalRequests == 0) return 0.0;
        return (double) hits.get() / totalRequests * 100;
    }

    public String getName() {
        return "LRU-with-TTL";
    }
}






