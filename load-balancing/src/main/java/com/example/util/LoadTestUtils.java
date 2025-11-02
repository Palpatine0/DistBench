package com.example.util;

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
}


