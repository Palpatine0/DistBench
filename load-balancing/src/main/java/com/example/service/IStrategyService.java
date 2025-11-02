package com.example.service;

import com.example.vo.TestResult;

/**
 * Interface for load balancing strategy services.
 * Each strategy service runs the three core scenarios and returns TestResult.
 */
public interface IStrategyService {
    
    /**
     * Run the heterogeneous nodes scenario with this strategy
     * @return test results including distribution, latency stats, and metrics
     */
    TestResult runHeterogeneousNodes();
    
    /**
     * Run the hot key scenario with this strategy
     * @return test results including hot key distribution and metrics
     */
    TestResult runHotKey();
    
    /**
     * Run the partial failure scenario with this strategy
     * @return test results including phase breakdown and failure metrics
     */
    TestResult runPartialFailure();
}

