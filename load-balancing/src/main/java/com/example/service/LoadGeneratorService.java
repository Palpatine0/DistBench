package com.example.service;

import com.example.strategy.LoadBalancerStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
public class LoadGeneratorService {
    
    @Autowired
    private WorkerService workerService;

    private final ExecutorService executor = Executors.newFixedThreadPool(100);

    /**
     * Generates concurrent load with specified parameters
     * @param requests the number of requests to generate
     * @param strategy the load balancing strategy to use
     * @param key the key for the request
     * @return list of response times and selected worker IDs
     */
    public List<RequestResult> generateLoad(int requests, LoadBalancerStrategy strategy, String key) {
        return generateLoad(requests, strategy, i -> key);
    }

    /**
     * Generates concurrent load, allowing a per-request key generator.
     */
    public List<RequestResult> generateLoad(int requests, LoadBalancerStrategy strategy, Function<Integer, String> keyGenerator) {
        List<CompletableFuture<RequestResult>> futures = new ArrayList<>(requests);
        for (int i = 1; i <= requests; i++) {
            final int requestIndex = i;
            futures.add(CompletableFuture.supplyAsync(() -> {
                String key = keyGenerator.apply(requestIndex);
                int workerId = strategy.selectWorker(key, workerService.getWorkerCount());
                long start = System.currentTimeMillis();
                boolean success = true;
                try {
                    workerService.processRequest(workerId, key);
                } catch (Exception e) {
                    success = false;
                }
                long duration = System.currentTimeMillis() - start;
                return new RequestResult(duration, workerId, success);
            }, executor));
        }
        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        executor.shutdownNow();
    }

    public static class RequestResult {
        private final long responseTime;
        private final int workerId;
        private final boolean success;

        public RequestResult(long responseTime, int workerId, boolean success) {
            this.responseTime = responseTime;
            this.workerId = workerId;
            this.success = success;
        }

        public long getResponseTime() {
            return responseTime;
        }

        public int getWorkerId() {
            return workerId;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}

