package com.example.scenario;

import com.example.strategy.LRUWithTTLStrategy;
import com.example.util.CacheTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Freshness Requirements Scenario.
 * Tests how strategies handle TTL and data freshness constraints.
 * Simulates rules that specify how up-to-date a response must be.
 * 
 * Key metrics:
 * - Expired item count
 * - Fresh hits vs stale/expired fetches
 * - Impact of TTL on hit rate
 */
@Component
public class FreshnessRequirementsScenario implements Scenario {

    @Autowired
    private LRUWithTTLStrategy lruWithTtlStrategy;

    // Scenario configuration
    private long ttlMs = 2000;  // 2 second TTL (short for testing expiration)
    private int cacheSize = 100;
    private int totalRequests = 500;
    private int uniqueKeys = 50;  // Fewer keys = more repeated access
    private boolean resetTtlOnAccess = true;
    private long requestPacingMs = 50;  // Delay between requests

    @Override
    public void setup() {
        lruWithTtlStrategy.setMaxSize(cacheSize);
        lruWithTtlStrategy.setTtlMs(ttlMs);
        lruWithTtlStrategy.setResetTtlOnAccess(resetTtlOnAccess);
        lruWithTtlStrategy.reset();
    }

    @Override
    public String getName() {
        return "freshness-requirements";
    }

    /**
     * Configure for short TTL test (frequent expirations).
     */
    public void configureShortTtl() {
        this.ttlMs = 1000;  // 1 second
        this.resetTtlOnAccess = false;
        this.requestPacingMs = 100;
    }

    /**
     * Configure for medium TTL test.
     */
    public void configureMediumTtl() {
        this.ttlMs = 5000;  // 5 seconds
        this.resetTtlOnAccess = true;
        this.requestPacingMs = 50;
    }

    /**
     * Configure for long TTL test (rare expirations).
     */
    public void configureLongTtl() {
        this.ttlMs = 30000;  // 30 seconds
        this.resetTtlOnAccess = true;
        this.requestPacingMs = 10;
    }

    /**
     * Configure for TTL reset behavior comparison.
     */
    public void configureNoTtlReset() {
        this.ttlMs = 3000;  // 3 seconds
        this.resetTtlOnAccess = false;
    }

    public void setTtlMs(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    public long getTtlMs() {
        return ttlMs;
    }

    public void setResetTtlOnAccess(boolean reset) {
        this.resetTtlOnAccess = reset;
    }

    public boolean isResetTtlOnAccess() {
        return resetTtlOnAccess;
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

    public void setRequestPacingMs(long pacingMs) {
        this.requestPacingMs = pacingMs;
    }

    public long getRequestPacingMs() {
        return requestPacingMs;
    }

    /**
     * Get key generator for this scenario.
     * Uses hot key pattern to ensure some keys are accessed frequently.
     */
    public Function<Integer, String> keyGenerator() {
        return CacheTestUtils.hotKeyGenerator(uniqueKeys);
    }
}
