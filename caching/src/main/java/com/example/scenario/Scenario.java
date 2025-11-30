package com.example.scenario;

/**
 * Interface for cache test scenarios
 * Each scenario configures the environment before execution
 */
public interface Scenario {
    
    /**
     * Setup the scenario configuration
     * Called before each test run
     */
    void setup();
    
    /**
     * Get the scenario name
     * @return Scenario identifier
     */
    String getName();
}

