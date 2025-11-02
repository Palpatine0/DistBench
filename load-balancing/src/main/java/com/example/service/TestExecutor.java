package com.example.service;

import com.example.config.WorkerConfig;
import com.example.strategy.LoadBalancerStrategy;
import com.example.util.LoadTestUtils;
import com.example.vo.LatencyStats;
import com.example.vo.TestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class TestExecutor {

    @Autowired
    private WorkerService workerService;

    @Autowired
    private WorkerConfig workerConfig;

    public TestResult executeTest(
            String scenario,
            String strategyName,
            int numRequests,
            LoadBalancerStrategy strategy,
            Function<Integer, String> keyGenerator,
            BiConsumer<Integer, String> onEachRequest
    ) {
        List<Long> latencies = new ArrayList<>();
        Map<Integer, Integer> workerCounts = new HashMap<>();
        Map<Integer, Integer> failureCountsByWorker = new HashMap<>();
        int failures = 0;

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= numRequests; i++) {
            String key = keyGenerator.apply(i);
            int workerId = strategy.selectWorker(key, workerConfig.getCount());

            long reqStart = System.currentTimeMillis();
            try {
                workerService.processRequest(workerId, key);
            } catch (Exception e) {
                failures++;
                failureCountsByWorker.merge(workerId, 1, Integer::sum);
            } finally {
                long latency = System.currentTimeMillis() - reqStart;
                latencies.add(latency);
                workerCounts.merge(workerId, 1, Integer::sum);
                if (onEachRequest != null) {
                    onEachRequest.accept(workerId, key);
                }
                if (i % 50 == 0) {
                    System.out.println("Progress: " + i + "/" + numRequests);
                }
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        Collections.sort(latencies);
        LatencyStats stats = new LatencyStats(
                latencies.get(0),
                latencies.get(latencies.size() - 1),
                latencies.stream().mapToLong(Long::longValue).average().orElse(0),
                LoadTestUtils.getPercentile(latencies, 50),
                LoadTestUtils.getPercentile(latencies, 95),
                LoadTestUtils.getPercentile(latencies, 99)
        );

        TestResult result = new TestResult();
        result.setScenario(scenario);
        result.setStrategy(strategyName);
        result.setTotalRequests(numRequests);
        result.setSuccessfulRequests(numRequests - failures);
        result.setFailedRequests(failures);
        result.setDurationMs(duration);
        result.setDistribution(LoadTestUtils.convertWorkerCounts(workerCounts, workerConfig.getCount()));
        result.setLatency(stats);
        result.setRawLatencies(latencies);

        if (!failureCountsByWorker.isEmpty()) {
            Map<String, Integer> failuresByWorker = new HashMap<>();
            failureCountsByWorker.forEach((id, count) -> failuresByWorker.put("worker" + id, count));
            result.addAdditionalMetric("failuresByWorker", failuresByWorker);
        }

        return result;
    }
}


