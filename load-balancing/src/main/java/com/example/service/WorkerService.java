package com.example.service;

import com.example.config.WorkerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class WorkerService {
    
    private final WorkerConfig workerConfig;
    private final Random random = new Random();

    @Autowired
    public WorkerService(WorkerConfig workerConfig) {
        this.workerConfig = workerConfig;
    }

    /**
     * Simulates processing a request by a specific worker
     * @param workerId the worker ID (1-indexed)
     * @return the processing time in milliseconds
     */
    public long processRequest(int workerId) {
        long start = System.currentTimeMillis();
        WorkerConfig.WorkerSettings settings = workerConfig.getWorkerSettings(workerId);
        int latencyMs = settings.getLatency();
        double failureRate = settings.getFailureRate();

        try {
            if (latencyMs > 0) {
                Thread.sleep(latencyMs);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        if (random.nextDouble() < failureRate) {
            throw new RuntimeException("Worker " + workerId + " failed during processing");
        }

        return System.currentTimeMillis() - start;
    }

    /**
     * Get the worker count
     */
    public int getWorkerCount() {
        return workerConfig.getCount();
    }
}

