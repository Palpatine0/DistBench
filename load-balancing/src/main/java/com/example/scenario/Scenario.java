package com.example.scenario;

import com.example.strategy.LoadBalancerStrategy;
import com.example.service.LoadGeneratorService;
import com.example.metrics.MetricsCollector;

public interface Scenario {
    /**
     * Runs the scenario with the given strategy
     * @param strategy the load balancing strategy to use
     * @param loadGenerator the load generator service
     * @param metricsCollector the metrics collector
     * @return metrics from the scenario run
     */
    MetricsCollector.Metrics run(LoadBalancerStrategy strategy, 
                                LoadGeneratorService loadGenerator, 
                                MetricsCollector metricsCollector);

    /**
     * Gets the name of the scenario
     * @return scenario name
     */
    String getName();
}

