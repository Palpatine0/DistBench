package com.example.vo;

/**
 * Value object for latency statistics
 * Reused from load-balancing module concept
 */
public class LatencyStats {
    
    private long min;
    private long max;
    private double average;
    private long p50;  // Median
    private long p95;  // 95th percentile
    private long p99;  // 99th percentile
    
    // Getters and setters
    public long getMin() {
        return min;
    }
    
    public void setMin(long min) {
        this.min = min;
    }
    
    public long getMax() {
        return max;
    }
    
    public void setMax(long max) {
        this.max = max;
    }
    
    public double getAverage() {
        return average;
    }
    
    public void setAverage(double average) {
        this.average = average;
    }
    
    public long getP50() {
        return p50;
    }
    
    public void setP50(long p50) {
        this.p50 = p50;
    }
    
    public long getP95() {
        return p95;
    }
    
    public void setP95(long p95) {
        this.p95 = p95;
    }
    
    public long getP99() {
        return p99;
    }
    
    public void setP99(long p99) {
        this.p99 = p99;
    }
}

