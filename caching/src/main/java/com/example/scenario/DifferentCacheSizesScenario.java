package com.example.scenario;

import com.example.strategy.LRUWithTTLStrategy;
import com.example.util.CacheTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Different Cache Sizes Scenario.
 * Tests how strategies perform with varying cache capacities.
 * Simulates cache tiers or nodes with unequal storage capacity.
 * 
 * Key metrics:
 * - Hit rate at different cache sizes
 * - Eviction frequency
 * - Impact of cache size on latency
 */
@Component
public class DifferentCacheSizesScenario implements Scenario {

    @Autowired
    private LRUWithTTLStrategy lruWithTtlStrategy;

    // Scenario configuration
    private int cacheSize = 50;  // Default: small cache
    private int totalRequests = 1000;
    private int uniqueKeys = 200;  // More keys than cache size to force evictions
    private long ttlMs = 30000;  // 30s TTL (long enough to not expire during test)

    @Override
    public void setup() {
        // Configure cache with scenario-specific size
        lruWithTtlStrategy.setMaxSize(cacheSize);
        lruWithTtlStrategy.setTtlMs(ttlMs);
        lruWithTtlStrategy.setResetTtlOnAccess(true);
        lruWithTtlStrategy.reset();
    }

    @Override
    public String getName() {
        return "different-cache-sizes";
    }

    /**
     * Configure for small cache test (high eviction scenario).
     */
    public void configureSmallCache() {
        this.cacheSize = 25;
        this.uniqueKeys = 200;
    }

    /**
     * Configure for medium cache test.
     */
    public void configureMediumCache() {
        this.cacheSize = 100;
        this.uniqueKeys = 200;
    }

    /**
     * Configure for large cache test (low eviction scenario).
     */
    public void configureLargeCache() {
        this.cacheSize = 250;
        this.uniqueKeys = 200;
    }

    /**
     * Set custom cache size.
     */
    public void setCacheSize(int size) {
        this.cacheSize = size;
    }

    public int getCacheSize() {
        return cacheSize;
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

    /**
     * Get key generator for this scenario.
     * Uses sequential pattern to ensure predictable cache behavior.
     */
    public Function<Integer, String> keyGenerator() {
        return CacheTestUtils.sequentialKeyGenerator(uniqueKeys);
    }
}
