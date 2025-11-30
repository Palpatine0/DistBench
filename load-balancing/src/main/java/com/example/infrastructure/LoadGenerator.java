package com.example.infrastructure;

import com.example.strategy.LoadBalancerStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.function.Function;

@Component
public class LoadGenerator {
    
    @Autowired
    private Worker worker;

    private final ExecutorService executor = Executors.newFixedThreadPool(100);

    /**
     * Generates concurrent load, allowing a per-request key generator.
     */
    public List<RequestRecord> generateLoad(int requests, LoadBalancerStrategy strategy, Function<Integer, String> keyGenerator) {
        return generateLoad(requests, strategy, keyGenerator, null, null);
    }

    /**
     * Generates concurrent load with optional pre/post request callbacks.
     * @param requests the number of requests to generate
     * @param strategy the load balancing strategy to use
     * @param keyGenerator function to generate keys per request
     * @param preRequest callback executed before processing (receives workerId)
     * @param postRequest callback executed after processing (receives workerId)
     * @return list of request records containing response times and worker IDs
     */
    public List<RequestRecord> generateLoad(
            int requests, 
            LoadBalancerStrategy strategy, 
            Function<Integer, String> keyGenerator,
            java.util.function.Consumer<Integer> preRequest,
            java.util.function.Consumer<Integer> postRequest) {
        List<CompletableFuture<RequestRecord>> futures = new ArrayList<>(requests);
        for (int i = 1; i <= requests; i++) {
            final int requestIndex = i;
            futures.add(CompletableFuture.supplyAsync(() -> {
                String key = keyGenerator.apply(requestIndex);
                int workerId = strategy.selectWorker(key, worker.getWorkerCount());
                
                if (preRequest != null) {
                    preRequest.accept(workerId);
                }
                
                long start = System.currentTimeMillis();
                boolean success = true;
                try {
                    worker.processRequest(workerId, key);
                } catch (Exception e) {
                    success = false;
                } finally {
                    if (postRequest != null) {
                        postRequest.accept(workerId);
                    }
                }
                long duration = System.currentTimeMillis() - start;
                return new RequestRecord(duration, workerId, success);
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

    public static class RequestRecord {
        private final long responseTime;
        private final int workerId;
        private final boolean success;

        public RequestRecord(long responseTime, int workerId, boolean success) {
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