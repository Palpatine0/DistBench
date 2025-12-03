package com.example.scenario;

import com.example.infrastructure.Worker;
import com.example.strategy.LRUWithTTLStrategy;
import com.example.util.CacheTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Network Delays Scenario.
 * Tests how strategies perform with varying backend latencies.
 * Demonstrates the value of caching when backend is slow.
 * 
 * Key metrics:
 * - Latency reduction from caching
 * - Backend fetch count
 * - P95/P99 latency improvements
 */
@Component
public class NetworkDelaysScenario implements Scenario {

    @Autowired
    private LRUWithTTLStrategy lruWithTtlStrategy;

    @Autowired
    private Worker worker;

    // Scenario configuration
    private long backendLatencyMs = 200;  // Simulated network delay
    private int jitterMs = 20;  // Latency variance
    private int cacheSize = 100;
    private int totalRequests = 500;
    private int uniqueKeys = 80;  // Fewer than cache size for good hit rate
    private long ttlMs = 30000;  // 30s TTL

    @Override
    public void setup() {
        // Configure cache
        lruWithTtlStrategy.setMaxSize(cacheSize);
        lruWithTtlStrategy.setTtlMs(ttlMs);
        lruWithTtlStrategy.setResetTtlOnAccess(true);
        lruWithTtlStrategy.reset();

        // Configure backend worker
        worker.setBaseLatency(backendLatencyMs);
        worker.setJitter(jitterMs);
        worker.resetStats();
    }

    @Override
    public String getName() {
        return "network-delays";
    }

    /**
     * Configure for fast backend test (minimal caching benefit).
     */
    public void configureFastBackend() {
        this.backendLatencyMs = 10;
        this.jitterMs = 2;
    }

    /**
     * Configure for moderate backend latency.
     */
    public void configureModerateBackend() {
        this.backendLatencyMs = 100;
        this.jitterMs = 10;
    }

    /**
     * Configure for slow backend test (maximum caching benefit).
     */
    public void configureSlowBackend() {
        this.backendLatencyMs = 500;
        this.jitterMs = 50;
    }

    /**
     * Configure for high-jitter network conditions.
     */
    public void configureHighJitter() {
        this.backendLatencyMs = 200;
        this.jitterMs = 100;  // ±100ms variance
    }

    public void setBackendLatencyMs(long latencyMs) {
        this.backendLatencyMs = latencyMs;
    }

    public long getBackendLatencyMs() {
        return backendLatencyMs;
    }

    public void setJitterMs(int jitterMs) {
        this.jitterMs = jitterMs;
    }

    public int getJitterMs() {
        return jitterMs;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setUniqueKeys(int uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
    }

    public int getUniqueKeys() {
        return uniqueKeys;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Get key generator for this scenario.
     * Uses random pattern for realistic access distribution.
     */
    public Function<Integer, String> keyGenerator() {
        return CacheTestUtils.randomKeyGenerator(uniqueKeys);
    }
}
