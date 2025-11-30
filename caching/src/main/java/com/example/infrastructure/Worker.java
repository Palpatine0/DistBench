package com.example.infrastructure;

import org.springframework.stereotype.Component;

/**
 * Simulates a backend service/database worker
 * Same concept as Worker in load-balancing module
 * Uses Thread.sleep() to simulate network latency and processing time
 */
@Component
public class Worker {
    
    private long baseLatencyMs = 100;
    private int jitterMs = 10;
    
    // TODO: Implement
    // - fetchData(key) simulates backend fetch with latency
    // - Configurable latency per scenario
    // - Random jitter (±10ms) to simulate real-world variance
    // - Thread.sleep() for simulation
    
    /**
     * Fetch data from backend (simulated)
     * @param key The data key to fetch
     * @return The fetched value
     */
    public String fetchData(String key) {
        // TODO: Simulate network delay with Thread.sleep(baseLatency + jitter)
        // Return simulated data
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    public void setBaseLatency(long latencyMs) {
        this.baseLatencyMs = latencyMs;
    }
    
    public long getBaseLatency() {
        return baseLatencyMs;
    }
}

