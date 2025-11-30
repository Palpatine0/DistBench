package com.example.vo;

/**
 * Value object for cache-specific statistics
 */
public class CacheStats {
    
    private int currentSize;
    private int maxSize;
    private int evictions;
    private double hitRate;
    private int expiredItems;
    private int staleHits;  // For stale-while-revalidate
    private int coalescedRequests;  // For request coalescing
    
    // Getters and setters
    public int getCurrentSize() {
        return currentSize;
    }
    
    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }
    
    public int getMaxSize() {
        return maxSize;
    }
    
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
    
    public int getEvictions() {
        return evictions;
    }
    
    public void setEvictions(int evictions) {
        this.evictions = evictions;
    }
    
    public double getHitRate() {
        return hitRate;
    }
    
    public void setHitRate(double hitRate) {
        this.hitRate = hitRate;
    }
    
    public int getExpiredItems() {
        return expiredItems;
    }
    
    public void setExpiredItems(int expiredItems) {
        this.expiredItems = expiredItems;
    }
    
    public int getStaleHits() {
        return staleHits;
    }
    
    public void setStaleHits(int staleHits) {
        this.staleHits = staleHits;
    }
    
    public int getCoalescedRequests() {
        return coalescedRequests;
    }
    
    public void setCoalescedRequests(int coalescedRequests) {
        this.coalescedRequests = coalescedRequests;
    }
}

