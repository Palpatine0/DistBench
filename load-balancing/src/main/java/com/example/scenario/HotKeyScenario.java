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

@Component
public class HotKeyScenario implements Scenario {
    
    @Autowired
    private WorkerConfig workerConfig;
    
    private final Random random = new Random();

    @Override
    public MetricsCollector.Metrics run(LoadBalancerStrategy strategy, 
                                        LoadGeneratorService loadGenerator, 
                                        MetricsCollector metricsCollector) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

