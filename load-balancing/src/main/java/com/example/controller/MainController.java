package com.example.controller;

import com.example.config.WorkerConfig;
import com.example.metrics.MetricsCollector;
import com.example.scenario.Scenario;
import com.example.scenario.HeterogeneousNodesScenario;
import com.example.scenario.HotKeyScenario;
import com.example.scenario.PartialFailureScenario;
import com.example.service.LoadGeneratorService;
import com.example.service.WorkerService;
import com.example.strategy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MainController {

    @Autowired
    private WorkerService workerService;

    @Autowired
    private LoadGeneratorService loadGeneratorService;

    @Autowired
    private WorkerConfig workerConfig;

    private final Map<String, LoadBalancerStrategy> strategies;
    private final Map<String, Scenario> scenarios;

    @Autowired
    public MainController(List<LoadBalancerStrategy> strategyList, List<Scenario> scenarioList) {
        this.strategies = new HashMap<>();
        this.scenarios = new HashMap<>();

        // Map strategies by canonical names without invoking their getName() methods
        for (LoadBalancerStrategy s : strategyList) {
            if (s instanceof RoundRobinStrategy) {
                strategies.put("round-robin", s);
            } else if (s instanceof LeastRequestStrategy) {
                strategies.put("least-request", s);
            } else if (s instanceof ConsistentHashStrategy) {
                strategies.put("consistent-hash", s);
            }
        }

        // Map scenarios by expected names without calling getName()
        for (Scenario sc : scenarioList) {
            if (sc instanceof HeterogeneousNodesScenario) {
                scenarios.put("heterogeneous", sc);
            } else if (sc instanceof HotKeyScenario) {
                scenarios.put("hot-key", sc);
            } else if (sc instanceof PartialFailureScenario) {
                scenarios.put("partial-failure", sc);
            }
        }
    }

    /**
     * 1. Load balancer endpoint (what load generator calls)
     */
    @GetMapping("/request")
    public ResponseEntity<?> handleRequest(@RequestParam String key, @RequestParam(required = false, defaultValue = "round-robin") String strategy) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 2. Run a specific scenario
     */
    @PostMapping("/scenario/run")
    public ResponseEntity<?> runScenario(
            @RequestParam String scenarioName,
            @RequestParam String strategy
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 3. Run all combinations (full test suite)
     */
    @PostMapping("/test/run-all")
    public ResponseEntity<?> runAllTests() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 4. Get metrics for a completed test
     */
    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics(
            @RequestParam String scenarioName,
            @RequestParam String strategy
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 5. Configure worker behavior (for testing)
     */
    @PostMapping("/worker/configure")
    public ResponseEntity<?> configureWorker(
            @RequestParam int workerId,
            @RequestParam int latency,
            @RequestParam(required = false, defaultValue = "0.0") Double failureRate
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

