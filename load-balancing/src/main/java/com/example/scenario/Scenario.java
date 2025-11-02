package com.example.scenario;

import java.util.function.Function;

public interface Scenario {
    /**
     * Gets the name of the scenario
     * @return scenario name
     */
    String getName();
    
    /**
     * Sets up the worker configuration for this scenario
     */
    void setup();
    
    /**
     * Gets the total number of requests for this scenario
     * @return total request count
     */
    int getTotalRequests();
    
    /**
     * Provides a key generator function that maps request index to request key
     * @return function that takes request index (1-based) and returns key string
     */
    Function<Integer, String> keyGenerator();
}

