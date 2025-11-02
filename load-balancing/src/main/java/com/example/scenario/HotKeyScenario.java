package com.example.scenario;

import com.example.config.WorkerConfig;
import com.example.metrics.MetricsCollector;
import com.example.service.LoadGeneratorService;
import com.example.service.LoadGeneratorService.RequestResult;
import com.example.strategy.LoadBalancerStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
public class HotKeyScenario implements Scenario {
    
    @Autowired
    private WorkerConfig workerConfig;
    
    private final Random random = new Random();

    @Override
    public MetricsCollector.Metrics run(LoadBalancerStrategy strategy, 
                                        LoadGeneratorService loadGenerator, 
                                        MetricsCollector metricsCollector) {
        setup();
        String scenarioName = getName();
        String strategyName = strategy.getName();
        List<RequestResult> results = loadGenerator.generateLoad(getTotalRequests(), strategy, keyGenerator());
        for (RequestResult r : results) {
            metricsCollector.recordRequest(scenarioName, strategyName, r.getResponseTime());
            if (r.isSuccess()) {
                metricsCollector.recordSuccess(scenarioName, strategyName);
            } else {
                metricsCollector.recordFailure(scenarioName, strategyName);
            }
            metricsCollector.recordWorkerLoad(scenarioName, strategyName, r.getWorkerId());
        }
        return metricsCollector.getMetrics(scenarioName, strategyName);
    }

    @Override
    public String getName() {
        return "hot-key";
    }

    public void setup() {
        workerConfig.setCount(3);
        workerConfig.updateWorkerSettings(1, 100, 0.0);
        workerConfig.updateWorkerSettings(2, 100, 0.0);
        workerConfig.updateWorkerSettings(3, 100, 0.0);
    }

    public int getTotalRequests() {
        return 300;
    }

    public int getHotKeyTarget() {
        return 240;
    }

    public Function<Integer, String> keyGenerator() {
        final int hotKeyTarget = getHotKeyTarget();
        return i -> i <= hotKeyTarget ? "popular" : ("key-" + (1 + random.nextInt(100)));
    }

    public BiConsumer<Integer, String> popularTracker(java.util.Map<Integer, Integer> popularWorkerCounts) {
        return (workerId, key) -> {
            if ("popular".equals(key)) {
                popularWorkerCounts.merge(workerId, 1, Integer::sum);
            }
        };
    }
}

