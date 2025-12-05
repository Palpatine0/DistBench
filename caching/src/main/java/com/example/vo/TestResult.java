package com.example.vo;

import com.example.util.CacheTestUtils;

/**
 * Value object representing test execution results
 * Similar to load-balancing module's TestResult
 */
public class TestResult {
    
    private String scenario;
    private String strategy;
    private int totalRequests;
    private int cacheHits;
    private int cacheMisses;
    private int backendFetches;
    private long durationMs;
    private CacheStats cacheStats;
    private LatencyStats latency;
    
    // TODO: Add more fields as needed
    // - evictionCount
    // - coalesced requests count (for request coalescing)
    // - stale hits count (for stale-while-revalidate)
    
    // Getters and setters
    public String getScenario() {
        return scenario;
    }
    
    public void setScenario(String scenario) {
        this.scenario = scenario;
    }
    
    public String getStrategy() {
        return strategy;
    }
    
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
    
    public int getTotalRequests() {
        return totalRequests;
    }
    
    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }
    
    public int getCacheHits() {
        return cacheHits;
    }
    
    public void setCacheHits(int cacheHits) {
        this.cacheHits = cacheHits;
    }
    
    public int getCacheMisses() {
        return cacheMisses;
    }
    
    public void setCacheMisses(int cacheMisses) {
        this.cacheMisses = cacheMisses;
    }
    
    public int getBackendFetches() {
        return backendFetches;
    }
    
    public void setBackendFetches(int backendFetches) {
        this.backendFetches = backendFetches;
    }
    
    public long getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
    
    public CacheStats getCacheStats() {
        return cacheStats;
    }
    
    public void setCacheStats(CacheStats cacheStats) {
        this.cacheStats = cacheStats;
    }
    
    public LatencyStats getLatency() {
        return latency;
    }
    
    public void setLatency(LatencyStats latency) {
        this.latency = latency;
    }
    
    public double getHitRate() {
        if (totalRequests == 0) return 0.0;
        return (double) cacheHits / totalRequests * 100;
    }

    /**
     * Calculate throughput in requests per second.
     * @return Throughput (total requests / duration)
     */
    public double getThroughputRps() {
        return CacheTestUtils.calculateThroughput(totalRequests, durationMs);
    }
}

