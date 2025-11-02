package com.example.strategy;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RoundRobinStrategy implements LoadBalancerStrategy {
    private final AtomicInteger currentWorker = new AtomicInteger(0);

    @Override
    public int selectWorker(String key, int totalWorkers) {
        if (totalWorkers <= 0) {
            throw new IllegalArgumentException("totalWorkers must be > 0");
        }
        // Thread-safe increment and wrap around
        return (currentWorker.getAndIncrement() % totalWorkers) + 1;
    }

    @Override
    public String getName() {
        return "round-robin";
    }

    /**
     * Reset the internal pointer so the next selection starts from worker-1.
     */
    public void reset() {
        currentWorker.set(0);
    }
}

