package com.example.strategy;

import org.springframework.stereotype.Component;

@Component
public class RoundRobinStrategy implements LoadBalancerStrategy {
    private int currentWorker = 0;

    @Override
    public int selectWorker(String key, int totalWorkers) {
        if (totalWorkers <= 0) {
            throw new IllegalArgumentException("totalWorkers must be > 0");
        }
        currentWorker = (currentWorker % totalWorkers) + 1;
        return currentWorker;
    }

    @Override
    public String getName() {
        return "round-robin";
    }

    /**
     * Reset the internal pointer so the next selection starts from worker-1.
     */
    public void reset() {
        currentWorker = 0;
    }
}

