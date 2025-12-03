package com.example.service.impl;

import com.example.infrastructure.LoadGenerator;
import com.example.infrastructure.Worker;
import com.example.scenario.DifferentCacheSizesScenario;
import com.example.scenario.FreshnessRequirementsScenario;
import com.example.scenario.NetworkDelaysScenario;
import com.example.service.IStrategyService;
import com.example.strategy.LRUWithTTLStrategy;
import com.example.util.CacheTestUtils;
import com.example.vo.CacheStats;
import com.example.vo.LatencyStats;
import com.example.vo.RequestRecord;
import com.example.vo.TestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for LRU with TTL strategy.
 * Orchestrates scenario execution and result building.
 */
@Service
public class LruWithTtlServiceImpl implements IStrategyService {

    @Autowired
    private LRUWithTTLStrategy lruWithTtlStrategy;

    @Autowired
    private LoadGenerator loadGenerator;

    @Autowired
    private Worker worker;

    @Autowired
    private DifferentCacheSizesScenario differentCacheSizesScenario;

    @Autowired
    private FreshnessRequirementsScenario freshnessRequirementsScenario;

    @Autowired
    private NetworkDelaysScenario networkDelaysScenario;

    @Override
    public TestResult runDifferentCacheSizes() {
        // Reset worker stats
        worker.resetStats();
        
        // Setup scenario (uses default medium cache size)
        differentCacheSizesScenario.configureMediumCache();
        differentCacheSizesScenario.setup();

        // Execute test
        List<RequestRecord> results = loadGenerator.generateLoad(
            differentCacheSizesScenario.getTotalRequests(),
            lruWithTtlStrategy,
            differentCacheSizesScenario.keyGenerator()
        );

        return buildTestResult(
            differentCacheSizesScenario.getName(),
            results,
            differentCacheSizesScenario.getCacheSize()
        );
    }

    @Override
    public TestResult runFreshnessRequirements() {
        // Reset worker stats
        worker.resetStats();
        
        // Setup scenario (uses default medium TTL)
        freshnessRequirementsScenario.configureMediumTtl();
        freshnessRequirementsScenario.setup();

        // Execute test with pacing to allow TTL behavior
        List<RequestRecord> results = loadGenerator.generateLoadWithPacing(
            freshnessRequirementsScenario.getTotalRequests(),
            lruWithTtlStrategy,
            freshnessRequirementsScenario.keyGenerator(),
            freshnessRequirementsScenario.getRequestPacingMs()
        );

        return buildTestResult(
            freshnessRequirementsScenario.getName(),
            results,
            lruWithTtlStrategy.getMaxSize()
        );
    }

    @Override
    public TestResult runNetworkDelays() {
        // Reset worker stats
        worker.resetStats();
        
        // Setup scenario (uses default moderate backend)
        networkDelaysScenario.configureModerateBackend();
        networkDelaysScenario.setup();

        // Execute test
        List<RequestRecord> results = loadGenerator.generateLoad(
            networkDelaysScenario.getTotalRequests(),
            lruWithTtlStrategy,
            networkDelaysScenario.keyGenerator()
        );

        return buildTestResult(
            networkDelaysScenario.getName(),
            results,
            networkDelaysScenario.getCacheSize()
        );
    }

    /**
     * Build TestResult from request records.
     */
    private TestResult buildTestResult(String scenario, List<RequestRecord> results, int cacheSize) {
        TestResult testResult = new TestResult();
        testResult.setScenario(scenario);
        testResult.setStrategy(lruWithTtlStrategy.getName());
        testResult.setTotalRequests(results.size());

        // Count hits, misses, and backend fetches
        int hits = 0;
        int misses = 0;
        int backendFetches = 0;
        List<Long> latencies = new ArrayList<>();

        for (RequestRecord record : results) {
            latencies.add(record.getLatencyMs());
            if (record.isCacheHit()) {
                hits++;
            } else {
                misses++;
            }
            if (record.isBackendFetch()) {
                backendFetches++;
            }
        }

        testResult.setCacheHits(hits);
        testResult.setCacheMisses(misses);
        testResult.setBackendFetches(backendFetches);

        // Calculate latency stats
        Collections.sort(latencies);
        LatencyStats latencyStats = CacheTestUtils.calculateLatencyStats(latencies);
        testResult.setLatency(latencyStats);

        // Calculate duration (sum of all latencies is not accurate for concurrent requests)
        // Use max latency as approximation for wall-clock time
        long duration = latencies.stream().mapToLong(Long::longValue).sum();
        testResult.setDurationMs(duration);

        // Build cache stats
        CacheStats cacheStats = new CacheStats();
        cacheStats.setMaxSize(cacheSize);
        cacheStats.setCurrentSize(lruWithTtlStrategy.size());
        cacheStats.setHitRate(CacheTestUtils.calculateHitRate(hits, results.size()));
        cacheStats.setEvictions(lruWithTtlStrategy.getEvictions());
        cacheStats.setExpiredItems(lruWithTtlStrategy.getExpirations());
        testResult.setCacheStats(cacheStats);

        return testResult;
    }

    /**
     * Run different cache sizes with specific size parameter.
     */
    public TestResult runDifferentCacheSizesWithSize(int cacheSize) {
        worker.resetStats();
        
        differentCacheSizesScenario.setCacheSize(cacheSize);
        differentCacheSizesScenario.setup();

        List<RequestRecord> results = loadGenerator.generateLoad(
            differentCacheSizesScenario.getTotalRequests(),
            lruWithTtlStrategy,
            differentCacheSizesScenario.keyGenerator()
        );

        return buildTestResult(
            differentCacheSizesScenario.getName() + "-size-" + cacheSize,
            results,
            cacheSize
        );
    }

    /**
     * Run freshness test with specific TTL.
     */
    public TestResult runFreshnessWithTtl(long ttlMs, boolean resetOnAccess) {
        worker.resetStats();
        
        freshnessRequirementsScenario.setTtlMs(ttlMs);
        freshnessRequirementsScenario.setResetTtlOnAccess(resetOnAccess);
        freshnessRequirementsScenario.setup();

        List<RequestRecord> results = loadGenerator.generateLoadWithPacing(
            freshnessRequirementsScenario.getTotalRequests(),
            lruWithTtlStrategy,
            freshnessRequirementsScenario.keyGenerator(),
            freshnessRequirementsScenario.getRequestPacingMs()
        );

        return buildTestResult(
            freshnessRequirementsScenario.getName() + "-ttl-" + ttlMs,
            results,
            lruWithTtlStrategy.getMaxSize()
        );
    }

    /**
     * Run network delays test with specific latency.
     */
    public TestResult runNetworkDelaysWithLatency(long backendLatencyMs) {
        worker.resetStats();
        
        networkDelaysScenario.setBackendLatencyMs(backendLatencyMs);
        networkDelaysScenario.setup();

        List<RequestRecord> results = loadGenerator.generateLoad(
            networkDelaysScenario.getTotalRequests(),
            lruWithTtlStrategy,
            networkDelaysScenario.keyGenerator()
        );

        return buildTestResult(
            networkDelaysScenario.getName() + "-latency-" + backendLatencyMs,
            results,
            networkDelaysScenario.getCacheSize()
        );
    }
}
