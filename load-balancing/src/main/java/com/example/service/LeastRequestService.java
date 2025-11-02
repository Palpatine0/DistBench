package com.example.service;

import com.example.config.WorkerConfig;
import com.example.vo.LatencyStats;
import com.example.vo.TestResult;
import com.example.strategy.LeastRequestStrategy;
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
public class LeastRequestService {

    @Autowired
    private WorkerConfig workerConfig;

    @Autowired
    private LeastRequestStrategy leastRequestStrategy;

    @Autowired
    private HeterogeneousNodesScenario heterogeneousNodesScenario;

    @Autowired
    private HotKeyScenario hotKeyScenario;

    @Autowired
    private PartialFailureScenario partialFailureScenario;

    @Autowired
    private TestExecutor testExecutor;

    public TestResult runHeterogeneousNodes() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TestResult runHotKey() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TestResult runPartialFailure() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
}


