package com.example.scenario;

import org.springframework.stereotype.Component;

/**
 * Network Delays Scenario
 * Tests how strategies perform with varying backend latencies
 * Simulates latency and variability in transit time that affect response time
 */
@Component
public class NetworkDelaysScenario implements Scenario {
    
    // TODO: Implement
    // - Configure different backend latencies (e.g., 50ms, 200ms, 500ms)
    // - Add jitter to simulate network variability
    // - Measure cache hit benefits vs backend fetch costs
    // - Test behavior with slow backends (importance of caching)
    
    @Override
    public void setup() {
        // Configure backend latencies
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public String getName() {
        return "network-delays";
    }
}

