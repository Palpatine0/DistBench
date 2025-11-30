package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for cache settings
 * Similar to WorkerConfig in load-balancing module
 * Binds to application.yml cache properties
 */
@Configuration
@ConfigurationProperties(prefix = "cache")
public class CacheConfig {
    
    private int maxSize = 100;
    private long ttlSeconds = 5;
    private boolean resetTtlOnAccess = true;
    private long backendLatencyMs = 100;
    
    // TODO: Add more configuration options
    // - Different cache sizes for tiers
    // - TTL configurations
    // - Backend latency settings
    
    // Getters and setters
    public int getMaxSize() {
        return maxSize;
    }
    
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
    
    public long getTtlSeconds() {
        return ttlSeconds;
    }
    
    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
    
    public boolean isResetTtlOnAccess() {
        return resetTtlOnAccess;
    }
    
    public void setResetTtlOnAccess(boolean resetTtlOnAccess) {
        this.resetTtlOnAccess = resetTtlOnAccess;
    }
    
    public long getBackendLatencyMs() {
        return backendLatencyMs;
    }
    
    public void setBackendLatencyMs(long backendLatencyMs) {
        this.backendLatencyMs = backendLatencyMs;
    }
}

