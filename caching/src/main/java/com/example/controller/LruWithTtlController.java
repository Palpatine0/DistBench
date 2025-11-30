package com.example.controller;

import com.example.service.impl.LruWithTtlServiceImpl;
import com.example.vo.TestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for LRU with TTL strategy
 */
@RestController
@RequestMapping("/api/lru-ttl")
public class LruWithTtlController {

    @Autowired
    private LruWithTtlServiceImpl lruWithTtlService;

    @GetMapping("/different-cache-sizes")
    public TestResult runDifferentCacheSizes() {
        return lruWithTtlService.runDifferentCacheSizes();
    }

    @GetMapping("/freshness-requirements")
    public TestResult runFreshnessRequirements() {
        return lruWithTtlService.runFreshnessRequirements();
    }

    @GetMapping("/network-delays")
    public TestResult runNetworkDelays() {
        return lruWithTtlService.runNetworkDelays();
    }
}

