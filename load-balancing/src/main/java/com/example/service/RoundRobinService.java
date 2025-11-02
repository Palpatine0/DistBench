package com.example.service;

import com.example.config.WorkerConfig;
import com.example.vo.LatencyStats;
import com.example.vo.TestResult;
import com.example.strategy.RoundRobinStrategy;
import com.example.util.LoadTestUtils;
import com.example.scenario.HeterogeneousNodesScenario;
import com.example.scenario.HotKeyScenario;
import com.example.scenario.PartialFailureScenario;
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
public class RoundRobinService {

    @Autowired
    private WorkerService workerService;

    @Autowired
    private WorkerConfig workerConfig;

    @Autowired
    private RoundRobinStrategy roundRobinStrategy;

    @Autowired
    private HeterogeneousNodesScenario heterogeneousNodesScenario;

    @Autowired
    private HotKeyScenario hotKeyScenario;

    @Autowired
    private PartialFailureScenario partialFailureScenario;

    public TestResult runHeterogeneousNodes() {
        heterogeneousNodesScenario.setup();
        roundRobinStrategy.reset();

        return executeTest(
                heterogeneousNodesScenario.getName(),
                heterogeneousNodesScenario.getTotalRequests(),
                heterogeneousNodesScenario.keyGenerator(),
                null
        );
    }

    public TestResult runHotKey() {
        hotKeyScenario.setup();
        roundRobinStrategy.reset();

        Map<Integer, Integer> popularWorkerCounts = new HashMap<>();
        TestResult result = executeTest(
                hotKeyScenario.getName(),
                hotKeyScenario.getTotalRequests(),
                hotKeyScenario.keyGenerator(),
                hotKeyScenario.popularTracker(popularWorkerCounts)
        );

        result.addAdditionalMetric("hotKeyRequests", hotKeyScenario.getHotKeyTarget());
        result.addAdditionalMetric("hotKeyDistribution", LoadTestUtils.convertWorkerCounts(popularWorkerCounts, workerConfig.getCount()));
        return result;
    }

    public TestResult runPartialFailure() {
        partialFailureScenario.setupPhase1();
        roundRobinStrategy.reset();

        TestResult phase1 = executeTest(
                partialFailureScenario.getName() + "-phase1",
                partialFailureScenario.getPhase1Requests(),
                partialFailureScenario.phase1KeyGenerator(),
                null
        );

        partialFailureScenario.enablePhase2Failure();

        TestResult phase2 = executeTest(
                partialFailureScenario.getName() + "-phase2",
                partialFailureScenario.getPhase2Requests(),
                partialFailureScenario.phase2KeyGenerator(),
                null
        );

        TestResult combined = new TestResult();
        combined.setScenario(partialFailureScenario.getName());
        combined.setStrategy("round-robin");
        combined.setTotalRequests(partialFailureScenario.getPhase1Requests() + partialFailureScenario.getPhase2Requests());
        combined.setSuccessfulRequests(phase1.getSuccessfulRequests() + phase2.getSuccessfulRequests());
        combined.setFailedRequests(phase1.getFailedRequests() + phase2.getFailedRequests());
        combined.setDurationMs(phase1.getDurationMs() + phase2.getDurationMs());

        Map<String, Integer> dist = new HashMap<>();
        if (phase1.getDistribution() != null) {
            phase1.getDistribution().forEach((k, v) -> dist.merge(k, v, Integer::sum));
        }
        if (phase2.getDistribution() != null) {
            phase2.getDistribution().forEach((k, v) -> dist.merge(k, v, Integer::sum));
        }
        combined.setDistribution(dist);

        List<Long> allLatencies = new ArrayList<>();
        if (phase1.getRawLatencies() != null) allLatencies.addAll(phase1.getRawLatencies());
        if (phase2.getRawLatencies() != null) allLatencies.addAll(phase2.getRawLatencies());
        if (!allLatencies.isEmpty()) {
            Collections.sort(allLatencies);
            LatencyStats stats = new LatencyStats(
                    allLatencies.get(0),
                    allLatencies.get(allLatencies.size() - 1),
                    allLatencies.stream().mapToLong(Long::longValue).average().orElse(0),
                    LoadTestUtils.getPercentile(allLatencies, 50),
                    LoadTestUtils.getPercentile(allLatencies, 95),
                    LoadTestUtils.getPercentile(allLatencies, 99)
            );
            combined.setLatency(stats);
        }

        combined.addAdditionalMetric("phase1", phase1);
        combined.addAdditionalMetric("phase2", phase2);
        Map<String, Integer> failuresByWorker = new HashMap<>();
        Object f1 = phase1.any().get("failuresByWorker");
        Object f2 = phase2.any().get("failuresByWorker");
        if (f1 instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> m = (Map<String, Integer>) f1;
            m.forEach((k, v) -> failuresByWorker.merge(k, v, Integer::sum));
        }
        if (f2 instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> m = (Map<String, Integer>) f2;
            m.forEach((k, v) -> failuresByWorker.merge(k, v, Integer::sum));
        }
        combined.addAdditionalMetric("failuresByWorker", failuresByWorker);

        return combined;
    }

    private TestResult executeTest(
            String scenario,
            int numRequests,
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
            int workerId = roundRobinStrategy.selectWorker(key, workerConfig.getCount());

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
        result.setStrategy("round-robin");
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


