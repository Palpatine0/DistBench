package com.example.metrics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MetricsCollector {
    private final Map<String, Metrics> metricsMap = new ConcurrentHashMap<>();

    public void recordRequest(String scenarioName, String strategy, long responseTime) {
        String key = createKey(scenarioName, strategy);
        metricsMap.computeIfAbsent(key, k -> new Metrics()).recordRequest(responseTime);
    }

    public void recordSuccess(String scenarioName, String strategy) {
        String key = createKey(scenarioName, strategy);
        metricsMap.computeIfAbsent(key, k -> new Metrics()).recordSuccess();
    }

    public void recordFailure(String scenarioName, String strategy) {
        String key = createKey(scenarioName, strategy);
        metricsMap.computeIfAbsent(key, k -> new Metrics()).recordFailure();
    }

    public void recordWorkerLoad(String scenarioName, String strategy, int workerId) {
        String key = createKey(scenarioName, strategy);
        metricsMap.computeIfAbsent(key, k -> new Metrics()).recordWorkerLoad(workerId);
    }

    public Metrics getMetrics(String scenarioName, String strategy) {
        String key = createKey(scenarioName, strategy);
        return metricsMap.computeIfAbsent(key, k -> new Metrics());
    }

    public void clearMetrics(String scenarioName, String strategy) {
        String key = createKey(scenarioName, strategy);
        metricsMap.remove(key);
    }

    public void clearAllMetrics() {
        metricsMap.clear();
    }

    private String createKey(String scenarioName, String strategy) {
        return scenarioName + ":" + strategy;
    }

    public static class Metrics {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final Map<Integer, AtomicInteger> workerLoads = new ConcurrentHashMap<>();

        public void recordRequest(long responseTime) {
            requestCount.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
        }

        public void recordSuccess() {
            successCount.incrementAndGet();
        }

        public void recordFailure() {
            failureCount.incrementAndGet();
        }

        public void recordWorkerLoad(int workerId) {
            workerLoads.computeIfAbsent(workerId, id -> new AtomicInteger(0)).incrementAndGet();
        }

        public int getRequestCount() {
            return requestCount.get();
        }

        public int getSuccessCount() {
            return successCount.get();
        }

        public int getFailureCount() {
            return failureCount.get();
        }

        public double getAverageResponseTime() {
            int count = requestCount.get();
            if (count == 0) return 0.0;
            return totalResponseTime.get() * 1.0 / count;
        }

        public double getSuccessRate() {
            int count = requestCount.get();
            if (count == 0) return 0.0;
            return successCount.get() * 1.0 / count;
        }

        public Map<Integer, Integer> getWorkerLoads() {
            Map<Integer, Integer> result = new ConcurrentHashMap<>();
            workerLoads.forEach((id, c) -> result.put(id, c.get()));
            return result;
        }

        public String toString() {
            return "Metrics{" +
                    "requests=" + requestCount.get() +
                    ", successes=" + successCount.get() +
                    ", failures=" + failureCount.get() +
                    ", avgMs=" + getAverageResponseTime() +
                    ", successRate=" + getSuccessRate() +
                    '}';
        }
    }
}

