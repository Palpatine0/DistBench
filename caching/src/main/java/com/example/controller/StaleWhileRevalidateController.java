package com.example.controller;

import com.example.service.impl.StaleWhileRevalidateServiceImpl;
import com.example.vo.TestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Stale-While-Revalidate cache strategy endpoints
 */
@RestController
@RequestMapping("/api/stale-while-revalidate")
public class StaleWhileRevalidateController {
    
    @Autowired
    private StaleWhileRevalidateServiceImpl service;
    
    @GetMapping("/different-cache-sizes")
    public TestResult runDifferentCacheSizes() {
        return service.runDifferentCacheSizes();
    }
    
    @GetMapping("/freshness-requirements")
    public TestResult runFreshnessRequirements() {
        return service.runFreshnessRequirements();
    }
    
    @GetMapping("/network-delays")
    public TestResult runNetworkDelays() {
        return service.runNetworkDelays();
    }
}

