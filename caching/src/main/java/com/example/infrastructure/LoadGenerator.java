package com.example.infrastructure;

import com.example.strategy.CacheStrategy;
import com.example.vo.RequestRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Generates concurrent load for cache testing.
 * Uses thread pool to simulate concurrent requests.
 * 
 * Features:
 * - Configurable concurrency level
 * - Support for different key generation patterns
 * - Collects detailed metrics per request
 */
@Component
public class LoadGenerator {

    @Autowired
    private Worker worker;

    private ExecutorService executorService;
    private int threadPoolSize = 100;

    public LoadGenerator() {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * Generate concurrent load against the cache.
     * 
     * @param totalRequests Number of requests to generate
     * @param strategy The cache strategy to test
     * @param keyGenerator Function to generate keys from request index
     * @return List of request results with metrics
     */
    public List<RequestRecord> generateLoad(
            int totalRequests,
            CacheStrategy strategy,
            Function<Integer, String> keyGenerator) {
        
        List<Future<RequestRecord>> futures = new ArrayList<>();
        
        for (int i = 1; i <= totalRequests; i++) {
            final int requestIndex = i;
            final String key = keyGenerator.apply(requestIndex);
            
            Future<RequestRecord> future = executorService.submit(() -> 
                executeRequest(key, strategy));
            futures.add(future);
        }
        
        // Collect results
        List<RequestRecord> results = new ArrayList<>();
        for (Future<RequestRecord> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                // Record failed request
                results.add(new RequestRecord(
                    "unknown", 0, false, false, false, false, false));
            }
        }
        
        return results;
    }

    /**
     * Generate load with controlled concurrency and pacing.
     * Useful for scenarios that need specific timing patterns.
     * 
     * @param totalRequests Number of requests to generate
     * @param strategy The cache strategy to test
     * @param keyGenerator Function to generate keys
     * @param delayBetweenRequestsMs Delay between submitting requests
     * @return List of request results
     */
    public List<RequestRecord> generateLoadWithPacing(
            int totalRequests,
            CacheStrategy strategy,
            Function<Integer, String> keyGenerator,
            long delayBetweenRequestsMs) {
        
        List<Future<RequestRecord>> futures = new ArrayList<>();
        
        for (int i = 1; i <= totalRequests; i++) {
            final int requestIndex = i;
            final String key = keyGenerator.apply(requestIndex);
            
            Future<RequestRecord> future = executorService.submit(() -> 
                executeRequest(key, strategy));
            futures.add(future);
            
            if (delayBetweenRequestsMs > 0 && i < totalRequests) {
                try {
                    Thread.sleep(delayBetweenRequestsMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // Collect results
        List<RequestRecord> results = new ArrayList<>();
        for (Future<RequestRecord> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                results.add(new RequestRecord(
                    "unknown", 0, false, false, false, false, false));
            }
        }
        
        return results;
    }

    /**
     * Execute a single cache request.
     * Tries to get from cache first, fetches from backend on miss.
     */
    private RequestRecord executeRequest(String key, CacheStrategy strategy) {
        long startTime = System.currentTimeMillis();
        boolean cacheHit = false;
        boolean backendFetch = false;
        boolean success = true;
        
        try {
            // Try to get from cache
            String value = strategy.get(key);
            
            if (value != null) {
                // Cache hit
                cacheHit = true;
            } else {
                // Cache miss - fetch from backend
                backendFetch = true;
                value = worker.fetchData(key);
                
                // Store in cache
                strategy.put(key, value);
            }
        } catch (Exception e) {
            success = false;
        }
        
        long latency = System.currentTimeMillis() - startTime;
        
        return new RequestRecord(key, latency, cacheHit, backendFetch, 
            false, false, success);
    }

    /**
     * Execute a single request with explicit backend latency.
     */
    public RequestRecord executeRequestWithLatency(
            String key, 
            CacheStrategy strategy, 
            long backendLatencyMs) {
        
        long startTime = System.currentTimeMillis();
        boolean cacheHit = false;
        boolean backendFetch = false;
        boolean success = true;
        
        try {
            String value = strategy.get(key);
            
            if (value != null) {
                cacheHit = true;
            } else {
                backendFetch = true;
                value = worker.fetchDataWithLatency(key, backendLatencyMs);
                strategy.put(key, value);
            }
        } catch (Exception e) {
            success = false;
        }
        
        long latency = System.currentTimeMillis() - startTime;
        
        return new RequestRecord(key, latency, cacheHit, backendFetch, 
            false, false, success);
    }

    public void setThreadPoolSize(int size) {
        this.threadPoolSize = size;
        shutdown();
        this.executorService = Executors.newFixedThreadPool(size);
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    public Worker getWorker() {
        return worker;
    }
}
