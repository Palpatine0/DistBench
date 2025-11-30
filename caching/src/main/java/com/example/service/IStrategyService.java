package com.example.service;

import com.example.vo.TestResult;

/**
 * Interface for cache strategy service implementations
 * Each strategy service runs all three scenarios
 */
public interface IStrategyService {
    
    /**
     * Run Different Cache Sizes scenario
     * Tests how the strategy performs with varying cache capacities
     * @return Test results with metrics
     */
    TestResult runDifferentCacheSizes();
    
    /**
     * Run Freshness Requirements scenario
     * Tests how the strategy handles TTL and freshness constraints
     * @return Test results with metrics
     */
    TestResult runFreshnessRequirements();
    
    /**
     * Run Network Delays scenario
     * Tests how the strategy performs with varying backend latencies
     * @return Test results with metrics
     */
    TestResult runNetworkDelays();
}

