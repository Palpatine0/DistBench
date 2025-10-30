package com.example.strategy;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LeastRequestStrategy implements LoadBalancerStrategy {
    private final ConcurrentHashMap<Integer, AtomicInteger> workerRequestCounts = new ConcurrentHashMap<>();

    @Override
    public int selectWorker(String key, int totalWorkers) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private int getRequestCount(int workerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // Method to reset counters (useful for testing)
    public void resetCounters() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

