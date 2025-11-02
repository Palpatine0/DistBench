package com.example.strategy;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LeastRequestStrategy implements LoadBalancerStrategy {
    private final ConcurrentHashMap<Integer, AtomicInteger> workerRequestCounts = new ConcurrentHashMap<>();

    @Override
    public int selectWorker(String key, int totalWorkers) {
        if (totalWorkers <= 0) {
            throw new IllegalArgumentException("totalWorkers must be > 0");
        }

        // Initialize all workers if not present
        for (int i = 1; i <= totalWorkers; i++) {
            workerRequestCounts.putIfAbsent(i, new AtomicInteger(0));
        }

        // Find worker with least requests
        int selectedWorker = 1;
        int minRequests = Integer.MAX_VALUE;

        for (int i = 1; i <= totalWorkers; i++) {
            int currentCount = getRequestCount(i);
            if (currentCount < minRequests) {
                minRequests = currentCount;
                selectedWorker = i;
            }
        }

        return selectedWorker;
    }

    /**
     * Get the current request count for a worker
     */
    private int getRequestCount(int workerId) {
        return workerRequestCounts.getOrDefault(workerId, new AtomicInteger(0)).get();
    }

    /**
     * Increment request count for a worker (called when request starts)
     */
    public void incrementRequestCount(int workerId) {
        workerRequestCounts.computeIfAbsent(workerId, id -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * Decrement request count for a worker (called when request completes)
     */
    public void decrementRequestCount(int workerId) {
        AtomicInteger count = workerRequestCounts.get(workerId);
        if (count != null) {
            count.decrementAndGet();
        }
    }

    @Override
    public String getName() {
        return "least-request";
    }

    /**
     * Reset counters (useful for testing and between scenario runs)
     */
    public void resetCounters() {
        workerRequestCounts.clear();
    }
}

