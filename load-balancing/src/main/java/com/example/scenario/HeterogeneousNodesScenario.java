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
public class HeterogeneousNodesScenario implements Scenario {
    
    @Autowired
    private WorkerConfig workerConfig;

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
        return "heterogeneous-nodes";
    }

    public void setup() {
        workerConfig.setCount(3);
        workerConfig.updateWorkerSettings(1, 50, 0.0);
        workerConfig.updateWorkerSettings(2, 100, 0.0);
        workerConfig.updateWorkerSettings(3, 200, 0.0);
    }

    public int getTotalRequests() {
        return 300;
    }

    public Function<Integer, String> keyGenerator() {
        return i -> "key-" + i;
    }
}

