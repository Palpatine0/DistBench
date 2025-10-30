package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "workers")
public class WorkerConfig {
    private int count = 3;
    private int defaultLatency = 100;
    private Map<Integer, WorkerSettings> workers = new HashMap<>();

    public WorkerConfig() {
        // no-args constructor required for @ConfigurationProperties binding
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
        // Ensure the workers map has at least [1..count] entries with defaults
        for (int i = 1; i <= count; i++) {
            workers.computeIfAbsent(i, id -> new WorkerSettings(defaultLatency, 0.0));
        }
    }

    public int getDefaultLatency() {
        return defaultLatency;
    }

    public void setDefaultLatency(int defaultLatency) {
        this.defaultLatency = defaultLatency;
        // Update existing default latencies only where latency was not explicitly set (0)
        workers.replaceAll((id, settings) -> {
            int latency = settings.latency == 0 ? defaultLatency : settings.latency;
            return new WorkerSettings(latency, settings.failureRate);
        });
    }

    public Map<Integer, WorkerSettings> getWorkers() {
        return workers;
    }

    public void setWorkers(Map<Integer, WorkerSettings> workers) {
        this.workers = workers != null ? workers : new HashMap<>();
    }

    public WorkerSettings getWorkerSettings(int workerId) {
        return workers.computeIfAbsent(workerId, id -> new WorkerSettings(defaultLatency, 0.0));
    }

    public void updateWorkerSettings(int workerId, int latency, double failureRate) {
        workers.put(workerId, new WorkerSettings(latency, failureRate));
    }

    // Support binding of properties like workers.worker1.latency via dedicated setters
    public void setWorker1(WorkerSettings settings) { if (settings != null) workers.put(1, normalize(settings)); }
    public void setWorker2(WorkerSettings settings) { if (settings != null) workers.put(2, normalize(settings)); }
    public void setWorker3(WorkerSettings settings) { if (settings != null) workers.put(3, normalize(settings)); }

    private WorkerSettings normalize(WorkerSettings in) {
        int latency = in.latency == 0 ? defaultLatency : in.latency;
        double failure = in.failureRate;
        return new WorkerSettings(latency, failure);
    }

    public static class WorkerSettings {
        private int latency;
        private double failureRate;

        public WorkerSettings() {
            // for configuration binding
        }

        public WorkerSettings(int latency, double failureRate) {
            this.latency = latency;
            this.failureRate = failureRate;
        }

        public int getLatency() {
            return latency;
        }

        public void setLatency(int latency) {
            this.latency = latency;
        }

        public double getFailureRate() {
            return failureRate;
        }

        public void setFailureRate(double failureRate) {
            this.failureRate = failureRate;
        }
    }
}

