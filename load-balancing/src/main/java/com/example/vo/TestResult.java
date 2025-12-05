package com.example.vo;

import com.example.util.LoadTestUtils;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({
    "scenario", "strategy", "totalRequests", "successfulRequests", "failedRequests",
    "durationMs", "throughputRps", "goodputRps", "fairnessIndex",
    "distribution", "latency"
})
public class TestResult {
    private String scenario;
    private String strategy;
    private int totalRequests;
    private int successfulRequests;
    private int failedRequests;
    private long durationMs;
    private Map<String, Integer> distribution;
    private LatencyStats latency;

    private final Map<String, Object> additionalMetrics = new HashMap<>();

    @JsonIgnore
    private transient List<Long> rawLatencies;

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public int getSuccessfulRequests() {
        return successfulRequests;
    }

    public void setSuccessfulRequests(int successfulRequests) {
        this.successfulRequests = successfulRequests;
    }

    public int getFailedRequests() {
        return failedRequests;
    }

    public void setFailedRequests(int failedRequests) {
        this.failedRequests = failedRequests;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public Map<String, Integer> getDistribution() {
        return distribution;
    }

    public void setDistribution(Map<String, Integer> distribution) {
        this.distribution = distribution;
    }

    public LatencyStats getLatency() {
        return latency;
    }

    public void setLatency(LatencyStats latency) {
        this.latency = latency;
    }

    /**
     * Calculate throughput in requests per second.
     * @return Throughput (total requests / duration)
     */
    public double getThroughputRps() {
        return LoadTestUtils.calculateThroughput(totalRequests, durationMs);
    }

    /**
     * Calculate goodput (successful requests per second).
     * @return Goodput (successful requests / duration)
     */
    public double getGoodputRps() {
        return LoadTestUtils.calculateGoodput(successfulRequests, durationMs);
    }

    /**
     * Calculate Jain's Fairness Index for load distribution.
     * Returns value between 0 and 1.0, where 1.0 = perfect fairness.
     * @return Jain's Fairness Index
     */
    public double getFairnessIndex() {
        return LoadTestUtils.calculateJainsFairnessIndex(distribution);
    }

    public void addAdditionalMetric(String key, Object value) {
        this.additionalMetrics.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> any() {
        return additionalMetrics;
    }

    @JsonIgnore
    public List<Long> getRawLatencies() {
        return rawLatencies;
    }

    @JsonIgnore
    public void setRawLatencies(List<Long> rawLatencies) {
        this.rawLatencies = rawLatencies;
    }
}