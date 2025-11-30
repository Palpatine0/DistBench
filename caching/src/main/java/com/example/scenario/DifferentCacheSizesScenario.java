package com.example.scenario;

import org.springframework.stereotype.Component;

/**
 * Different Cache Sizes Scenario
 * Tests how strategies perform with varying cache capacities
 * Simulates cache tiers or nodes with unequal storage capacity
 */
@Component
public class DifferentCacheSizesScenario implements Scenario {
    
    // TODO: Implement
    // - Configure different cache sizes (e.g., 50, 100, 200 items)
    // - Generate requests that exceed cache capacity
    // - Measure eviction rates and hit ratios
    
    @Override
    public void setup() {
        // Configure cache sizes
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public String getName() {
        return "different-cache-sizes";
    }
}

