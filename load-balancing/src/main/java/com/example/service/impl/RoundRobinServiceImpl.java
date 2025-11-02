package com.example.service.impl;

import com.example.config.WorkerConfig;
import com.example.infrastructure.LoadGenerator;
import com.example.infrastructure.LoadGenerator.RequestRecord;
import com.example.service.IStrategyService;
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
public class RoundRobinServiceImpl implements IStrategyService {

    @Autowired
    private LoadGenerator loadGenerator;

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

    @Override
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

    @Override
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

    @Override
    public TestResult runPartialFailure() {
        partialFailureScenario.setup();
        roundRobinStrategy.reset();

        return executeTest(
            partialFailureScenario.getName(),
            partialFailureScenario.getTotalRequests(),
            partialFailureScenario.keyGenerator(),
            null
        );
    }

    private TestResult executeTest(
        String scenario,
        int numRequests,
        Function<Integer, String> keyGenerator,
        BiConsumer<Integer, String> onEachRequest
    ) {
        long startTime = System.currentTimeMillis();

        // Use LoadGenerator for concurrent request execution
        List<RequestRecord> results = loadGenerator.generateLoad(numRequests, roundRobinStrategy, keyGenerator);

        long duration = System.currentTimeMillis() - startTime;

        // Process results
        List<Long> latencies = new ArrayList<>();
        Map<Integer, Integer> workerCounts = new HashMap<>();
        Map<Integer, Integer> failureCountsByWorker = new HashMap<>();
        int failures = 0;

        for (int i = 0; i < results.size(); i++) {
            RequestRecord result = results.get(i);
            latencies.add(result.getResponseTime());
            workerCounts.merge(result.getWorkerId(), 1, Integer::sum);

            if (!result.isSuccess()) {
                failures++;
                failureCountsByWorker.merge(result.getWorkerId(), 1, Integer::sum);
            }

            if (onEachRequest != null) {
                String key = keyGenerator.apply(i + 1);
                onEachRequest.accept(result.getWorkerId(), key);
            }
        }

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
        result.setStrategy(roundRobinStrategy.getName());
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


