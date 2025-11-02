package com.example.scenario;

import com.example.config.WorkerConfig;
import com.example.metrics.MetricsCollector;
import com.example.service.LoadGeneratorService;
import com.example.service.LoadGeneratorService.RequestResult;
import com.example.strategy.LoadBalancerStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class PartialFailureScenario implements Scenario {
    
    @Autowired
    private WorkerConfig workerConfig;

    @Override
    public MetricsCollector.Metrics run(LoadBalancerStrategy strategy, 
                                        LoadGeneratorService loadGenerator, 
                                        MetricsCollector metricsCollector) {
        setupPhase1();
        String scenarioName = getName();
        String strategyName = strategy.getName();

        List<RequestResult> phase1 = loadGenerator.generateLoad(getPhase1Requests(), strategy, phase1KeyGenerator());
        for (RequestResult r : phase1) {
            metricsCollector.recordRequest(scenarioName, strategyName, r.getResponseTime());
            if (r.isSuccess()) {
                metricsCollector.recordSuccess(scenarioName, strategyName);
            } else {
                metricsCollector.recordFailure(scenarioName, strategyName);
            }
            metricsCollector.recordWorkerLoad(scenarioName, strategyName, r.getWorkerId());
        }

        enablePhase2Failure();
        List<RequestResult> phase2 = loadGenerator.generateLoad(getPhase2Requests(), strategy, phase2KeyGenerator());
        for (RequestResult r : phase2) {
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
        return "partial-failure";
    }

    public void setupPhase1() {
        workerConfig.setCount(3);
        workerConfig.updateWorkerSettings(1, 100, 0.0);
        workerConfig.updateWorkerSettings(2, 100, 0.0);
        workerConfig.updateWorkerSettings(3, 100, 0.0);
    }

    public int getPhase1Requests() {
        return 100;
    }

    public Function<Integer, String> phase1KeyGenerator() {
        return i -> "key-" + i;
    }

    public void enablePhase2Failure() {
        workerConfig.updateWorkerSettings(2, 100, 0.5);
    }

    public int getPhase2Requests() {
        return 200;
    }

    public Function<Integer, String> phase2KeyGenerator() {
        return i -> "key-" + (100 + i);
    }
}

