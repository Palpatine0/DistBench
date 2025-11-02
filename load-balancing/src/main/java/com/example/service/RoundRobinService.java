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

@Service
public class RoundRobinService {

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

    @Autowired
    private TestExecutor testExecutor;

    public TestResult runHeterogeneousNodes() {
        heterogeneousNodesScenario.setup();
        roundRobinStrategy.reset();
        TestResult result = testExecutor.executeTest(
            heterogeneousNodesScenario.getName(),
            "round-robin",
            heterogeneousNodesScenario.getTotalRequests(),
            roundRobinStrategy,
            heterogeneousNodesScenario.keyGenerator(),
            null
        );
        return result;
    }

    public TestResult runHotKey() {
        hotKeyScenario.setup();
        roundRobinStrategy.reset();

        Map<Integer, Integer> popularWorkerCounts = new HashMap<>();
        TestResult result = testExecutor.executeTest(
            hotKeyScenario.getName(),
            "round-robin",
            hotKeyScenario.getTotalRequests(),
            roundRobinStrategy,
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

        TestResult phase1Result = testExecutor.executeTest(
            partialFailureScenario.getName() + "-phase1",
            "round-robin",
            partialFailureScenario.getPhase1Requests(),
            roundRobinStrategy,
            partialFailureScenario.phase1KeyGenerator(),
            null
        );

        partialFailureScenario.enablePhase2Failure();

        TestResult phase2Result = testExecutor.executeTest(
            partialFailureScenario.getName() + "-phase2",
            "round-robin",
            partialFailureScenario.getPhase2Requests(),
            roundRobinStrategy,
            partialFailureScenario.phase2KeyGenerator(),
            null
        );

        TestResult combinedResult = new TestResult();
        combinedResult.setScenario(partialFailureScenario.getName());
        combinedResult.setStrategy("round-robin");
        combinedResult.setTotalRequests(partialFailureScenario.getPhase1Requests() + partialFailureScenario.getPhase2Requests());
        combinedResult.setSuccessfulRequests(phase1Result.getSuccessfulRequests() + phase2Result.getSuccessfulRequests());
        combinedResult.setFailedRequests(phase1Result.getFailedRequests() + phase2Result.getFailedRequests());
        combinedResult.setDurationMs(phase1Result.getDurationMs() + phase2Result.getDurationMs());

        Map<String, Integer> dist = new HashMap<>();
        if (phase1Result.getDistribution() != null) {
            phase1Result.getDistribution().forEach((k, v) -> dist.merge(k, v, Integer::sum));
        }
        if (phase2Result.getDistribution() != null) {
            phase2Result.getDistribution().forEach((k, v) -> dist.merge(k, v, Integer::sum));
        }
        combinedResult.setDistribution(dist);

        List<Long> allLatencies = new ArrayList<>();
        if (phase1Result.getRawLatencies() != null) allLatencies.addAll(phase1Result.getRawLatencies());
        if (phase2Result.getRawLatencies() != null) allLatencies.addAll(phase2Result.getRawLatencies());
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
            combinedResult.setLatency(stats);
        }

        combinedResult.addAdditionalMetric("phase1", phase1Result);
        combinedResult.addAdditionalMetric("phase2", phase2Result);
        Map<String, Integer> failuresByWorker = new HashMap<>();
        Object f1 = phase1Result.any().get("failuresByWorker");
        Object f2 = phase2Result.any().get("failuresByWorker");
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
        combinedResult.addAdditionalMetric("failuresByWorker", failuresByWorker);

        return combinedResult;
    }

}


