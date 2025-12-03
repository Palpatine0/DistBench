package com.example.util;

import com.example.vo.LatencyStats;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Utility class for cache test calculations.
 * Provides latency statistics, key generation, and other test helpers.
 */
public class CacheTestUtils {

    private static final Random random = new Random();

    /**
     * Calculate latency percentiles from sorted latency list.
     * 
     * @param sortedLatencies List of latencies in ascending order
     * @return LatencyStats object with min, max, avg, and percentiles
     */
    public static LatencyStats calculateLatencyStats(List<Long> sortedLatencies) {
        if (sortedLatencies == null || sortedLatencies.isEmpty()) {
            LatencyStats stats = new LatencyStats();
            stats.setMin(0);
            stats.setMax(0);
            stats.setAverage(0);
            stats.setP50(0);
            stats.setP95(0);
            stats.setP99(0);
            return stats;
        }

        // Ensure sorted
        Collections.sort(sortedLatencies);

        LatencyStats stats = new LatencyStats();
        stats.setMin(sortedLatencies.get(0));
        stats.setMax(sortedLatencies.get(sortedLatencies.size() - 1));
        stats.setAverage(sortedLatencies.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0));
        stats.setP50(getPercentile(sortedLatencies, 50));
        stats.setP95(getPercentile(sortedLatencies, 95));
        stats.setP99(getPercentile(sortedLatencies, 99));

        return stats;
    }

    /**
     * Get a specific percentile from a sorted list.
     * 
     * @param sortedList Sorted list of values
     * @param percentile Percentile to get (0-100)
     * @return The value at the given percentile
     */
    public static long getPercentile(List<Long> sortedList, int percentile) {
        if (sortedList == null || sortedList.isEmpty()) {
            return 0;
        }
        int index = (int) Math.ceil(percentile / 100.0 * sortedList.size()) - 1;
        index = Math.max(0, Math.min(index, sortedList.size() - 1));
        return sortedList.get(index);
    }

    /**
     * Calculate cache hit rate.
     * 
     * @param hits Number of cache hits
     * @param total Total requests
     * @return Hit rate as percentage (0-100)
     */
    public static double calculateHitRate(int hits, int total) {
        if (total == 0) return 0.0;
        return (double) hits / total * 100;
    }

    /**
     * Generate key based on index and pattern.
     * 
     * @param index Request index (1-based)
     * @param pattern Key generation pattern
     * @param maxKeys Maximum number of unique keys (for bounded patterns)
     * @return Generated key
     */
    public static String generateKey(int index, KeyPattern pattern, int maxKeys) {
        switch (pattern) {
            case SEQUENTIAL:
                // Keys cycle through 1 to maxKeys
                return "key_" + ((index - 1) % maxKeys + 1);
                
            case RANDOM:
                // Random key within range
                return "key_" + (random.nextInt(maxKeys) + 1);
                
            case HOT_KEY:
                // 80% of requests go to 20% of keys (Zipf-like distribution)
                if (random.nextDouble() < 0.8) {
                    int hotKeyCount = Math.max(1, maxKeys / 5);
                    return "key_" + (random.nextInt(hotKeyCount) + 1);
                } else {
                    return "key_" + (random.nextInt(maxKeys) + 1);
                }
                
            case BURST:
                // Same key repeated in bursts of 10
                int burstKey = ((index - 1) / 10) % maxKeys + 1;
                return "key_" + burstKey;
                
            default:
                return "key_" + index;
        }
    }

    /**
     * Key generation patterns for testing different cache behaviors.
     */
    public enum KeyPattern {
        /** Keys cycle sequentially through a range */
        SEQUENTIAL,
        /** Keys selected randomly from a range */
        RANDOM,
        /** 80% of requests hit 20% of keys (Zipf-like) */
        HOT_KEY,
        /** Same key repeated in bursts */
        BURST
    }

    /**
     * Create a sequential key generator function.
     * 
     * @param maxKeys Maximum unique keys
     * @return Key generator function
     */
    public static java.util.function.Function<Integer, String> sequentialKeyGenerator(int maxKeys) {
        return index -> generateKey(index, KeyPattern.SEQUENTIAL, maxKeys);
    }

    /**
     * Create a random key generator function.
     * 
     * @param maxKeys Maximum unique keys
     * @return Key generator function
     */
    public static java.util.function.Function<Integer, String> randomKeyGenerator(int maxKeys) {
        return index -> generateKey(index, KeyPattern.RANDOM, maxKeys);
    }

    /**
     * Create a hot key generator function.
     * 
     * @param maxKeys Maximum unique keys
     * @return Key generator function
     */
    public static java.util.function.Function<Integer, String> hotKeyGenerator(int maxKeys) {
        return index -> generateKey(index, KeyPattern.HOT_KEY, maxKeys);
    }

    /**
     * Create a burst key generator function.
     * 
     * @param maxKeys Maximum unique keys
     * @return Key generator function
     */
    public static java.util.function.Function<Integer, String> burstKeyGenerator(int maxKeys) {
        return index -> generateKey(index, KeyPattern.BURST, maxKeys);
    }
}
