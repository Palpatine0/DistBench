package com.example.infrastructure;

import com.example.config.CacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulates a backend service/database worker.
 * Uses Thread.sleep() to simulate network latency and processing time.
 * 
 * Features:
 * - Configurable base latency
 * - Random jitter to simulate real-world variance
 * - Request counting for metrics
 */
@Component
public class Worker {

    @Autowired
    private CacheConfig cacheConfig;

    private long baseLatencyMs = 100;
    private int jitterMs = 10;
    private final Random random = new Random();
    private final AtomicInteger fetchCount = new AtomicInteger(0);

    /**
     * Fetch data from backend (simulated).
     * Simulates network delay with configurable latency and jitter.
     * 
     * @param key The data key to fetch
     * @return The fetched value (simulated as "value_for_{key}")
     */
    public String fetchData(String key) {
        long latency = baseLatencyMs;
        
        // Add jitter: ±jitterMs
        if (jitterMs > 0) {
            latency += random.nextInt(jitterMs * 2 + 1) - jitterMs;
        }
        
        // Ensure minimum latency of 1ms
        latency = Math.max(1, latency);
        
        try {
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        fetchCount.incrementAndGet();
        return "value_for_" + key;
    }

    /**
     * Fetch data with explicit latency override.
     * Used by scenarios to test different network conditions.
     * 
     * @param key The data key to fetch
     * @param latencyMs Explicit latency to use
     * @return The fetched value
     */
    public String fetchDataWithLatency(String key, long latencyMs) {
        long latency = latencyMs;
        
        // Add jitter: ±jitterMs
        if (jitterMs > 0) {
            latency += random.nextInt(jitterMs * 2 + 1) - jitterMs;
        }
        
        latency = Math.max(1, latency);
        
        try {
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        fetchCount.incrementAndGet();
        return "value_for_" + key;
    }

    public void setBaseLatency(long latencyMs) {
        this.baseLatencyMs = latencyMs;
    }

    public long getBaseLatency() {
        return baseLatencyMs;
    }

    public void setJitter(int jitterMs) {
        this.jitterMs = jitterMs;
    }

    public int getJitter() {
        return jitterMs;
    }

    public int getFetchCount() {
        return fetchCount.get();
    }

    public void resetStats() {
        fetchCount.set(0);
    }

    /**
     * Apply configuration from CacheConfig.
     */
    public void applyConfig() {
        if (cacheConfig != null) {
            this.baseLatencyMs = cacheConfig.getBackendLatencyMs();
        }
    }
}
