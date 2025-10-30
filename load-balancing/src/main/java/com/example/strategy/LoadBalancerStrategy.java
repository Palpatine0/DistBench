package com.example.strategy;

public interface LoadBalancerStrategy {
    /**
     * Selects a worker ID based on the load balancing strategy
     * @param key the request key (for hash-based strategies)
     * @param totalWorkers total number of workers
     * @return the selected worker ID (1-indexed)
     */
    int selectWorker(String key, int totalWorkers);

    /**
     * Gets the name of the strategy
     * @return strategy name
     */
    String getName();
}

