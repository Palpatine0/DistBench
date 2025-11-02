package com.example.scenario;

import com.example.config.WorkerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class PartialFailureScenario implements Scenario {
    
    @Autowired
    private WorkerConfig workerConfig;

    @Override
    public String getName() {
        return "partial-failure";
    }

    @Override
    public void setup() {
        workerConfig.setCount(3);
        workerConfig.updateWorkerSettings(1, 100, 0.0);
        workerConfig.updateWorkerSettings(2, 100, 0.5);  // 50% failure rate
        workerConfig.updateWorkerSettings(3, 100, 0.0);
    }

    @Override
    public int getTotalRequests() {
        return 300;
    }

    @Override
    public Function<Integer, String> keyGenerator() {
        return i -> "key-" + i;
    }
}

