package com.example.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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