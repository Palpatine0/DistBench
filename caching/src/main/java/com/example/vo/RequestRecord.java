package com.example.vo;

/**
 * Record of a single cache request
 * Used internally for collecting metrics during load generation
 */
public class RequestRecord {
    
    private final String key;
    private final long latencyMs;
    private final boolean cacheHit;
    private final boolean backendFetch;
    private final boolean wasStale;  // For stale-while-revalidate
    private final boolean wasCoalesced;  // For request coalescing
    private final boolean success;
    
    public RequestRecord(String key, long latencyMs, boolean cacheHit, boolean backendFetch, 
                        boolean wasStale, boolean wasCoalesced, boolean success) {
        this.key = key;
        this.latencyMs = latencyMs;
        this.cacheHit = cacheHit;
        this.backendFetch = backendFetch;
        this.wasStale = wasStale;
        this.wasCoalesced = wasCoalesced;
        this.success = success;
    }
    
    // Getters
    public String getKey() {
        return key;
    }
    
    public long getLatencyMs() {
        return latencyMs;
    }
    
    public boolean isCacheHit() {
        return cacheHit;
    }
    
    public boolean isBackendFetch() {
        return backendFetch;
    }
    
    public boolean wasStale() {
        return wasStale;
    }
    
    public boolean wasCoalesced() {
        return wasCoalesced;
    }
    
    public boolean isSuccess() {
        return success;
    }
}

