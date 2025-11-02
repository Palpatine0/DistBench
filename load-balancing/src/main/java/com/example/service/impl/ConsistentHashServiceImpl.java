package com.example.service.impl;

import com.example.config.WorkerConfig;
import com.example.service.IStrategyService;
import com.example.vo.TestResult;
import com.example.strategy.ConsistentHashStrategy;
import com.example.scenario.HeterogeneousNodesScenario;
import com.example.scenario.HotKeyScenario;
import com.example.scenario.PartialFailureScenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsistentHashServiceImpl implements IStrategyService {

    @Autowired
    private WorkerConfig workerConfig;

    @Autowired
    private ConsistentHashStrategy consistentHashStrategy;

    @Autowired
    private HeterogeneousNodesScenario heterogeneousNodesScenario;

    @Autowired
    private HotKeyScenario hotKeyScenario;

    @Autowired
    private PartialFailureScenario partialFailureScenario;

    @Override
    public TestResult runHeterogeneousNodes() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public TestResult runHotKey() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public TestResult runPartialFailure() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}


