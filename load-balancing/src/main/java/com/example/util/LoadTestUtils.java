package com.example.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LoadTestUtils {

    private LoadTestUtils() {}

    public static long getPercentile(List<Long> sorted, int percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index);
    }

    public static Map<String, Integer> convertWorkerCounts(Map<Integer, Integer> counts, int totalWorkers) {
        Map<String, Integer> result = new HashMap<>();
        counts.forEach((id, count) -> result.put("worker" + id, count));
        for (int i = 1; i <= totalWorkers; i++) {
            result.putIfAbsent("worker" + i, 0);
        }
        return result;
    }

    /**
     * Calculate throughput in requests per second.
     *
     * @param totalRequests Total number of requests
     * @param durationMs    Duration in milliseconds
     * @return Throughput in RPS (requests per second)
     */
    public static double calculateThroughput(int totalRequests, long durationMs) {
        if (durationMs <= 0) return 0.0;
        return totalRequests / (durationMs / 1000.0);
    }

    /**
     * Calculate goodput (successful requests per second).
     *
     * @param successfulRequests Number of successful requests
     * @param durationMs         Duration in milliseconds
     * @return Goodput in RPS
     */
    public static double calculateGoodput(int successfulRequests, long durationMs) {
        if (durationMs <= 0) return 0.0;
        return successfulRequests / (durationMs / 1000.0);
    }

    /**
     * Calculate Jain's Fairness Index for load distribution.
     * Formula: (Σxi)² / (n × Σxi²)
     * 
     * Returns a value between 1/n (worst) and 1.0 (perfect fairness).
     * A value of 1.0 means all workers received exactly equal load.
     *
     * @param distribution Map of worker ID to request count
     * @return Jain's Fairness Index (0 to 1.0), or 1.0 if empty/single worker
     */
    public static double calculateJainsFairnessIndex(Map<String, Integer> distribution) {
        if (distribution == null || distribution.isEmpty()) {
            return 1.0;
        }
        
        Collection<Integer> values = distribution.values();
        int n = values.size();
        
        if (n == 1) {
            return 1.0;
        }
        
        long sum = 0;
        long sumOfSquares = 0;
        
        for (int value : values) {
            sum += value;
            sumOfSquares += (long) value * value;
        }
        
        if (sumOfSquares == 0) {
            return 1.0;
        }
        
        // J = (Σxi)² / (n × Σxi²)
        return (double) (sum * sum) / (n * sumOfSquares);
    }

    /**
     * Calculate imbalance ratio (max/min distribution).
     * A value of 1.0 means perfect balance.
     *
     * @param distribution Map of worker ID to request count
     * @return Imbalance ratio, or 1.0 if cannot be calculated
     */
    public static double calculateImbalanceRatio(Map<String, Integer> distribution) {
        if (distribution == null || distribution.size() < 2) {
            return 1.0;
        }
        
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        
        for (int value : distribution.values()) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        
        if (min == 0) {
            return max == 0 ? 1.0 : Double.POSITIVE_INFINITY;
        }
        
        return (double) max / min;
    }
}


