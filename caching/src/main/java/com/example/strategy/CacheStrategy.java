package com.example.strategy;

/**
 * Interface for cache strategy implementations
 * Defines how cache operations (get, put, eviction) are handled
 */
public interface CacheStrategy {
    
    /**
     * Retrieve a value from cache
     * @param key The cache key
     * @return The cached value, or null if not found or expired
     */
    String get(String key);
    
    /**
     * Store a value in cache
     * @param key The cache key
     * @param value The value to cache
     */
    void put(String key, String value);
    
    /**
     * Reset the cache to initial state
     * Called before each test scenario
     */
    void reset();
    
    /**
     * Get current cache size
     * @return Number of items in cache
     */
    int size();
}

