package com.example.service.impl;

import com.example.service.IStrategyService;
import com.example.strategy.StaleWhileRevalidateStrategy;
import com.example.vo.TestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service implementation for Stale-While-Revalidate strategy
 * Orchestrates scenario execution and builds test results
 */
@Service
public class StaleWhileRevalidateServiceImpl implements IStrategyService {
    
    @Autowired
    private StaleWhileRevalidateStrategy strategy;
    
    @Override
    public TestResult runDifferentCacheSizes() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public TestResult runFreshnessRequirements() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public TestResult runNetworkDelays() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

