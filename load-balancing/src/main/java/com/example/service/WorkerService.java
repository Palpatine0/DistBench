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
     * Process a request on a worker, simulating latency, jitter, and failures.
     */
    public String processRequest(int workerId, String key) throws Exception {
        WorkerConfig.WorkerSettings settings = workerConfig.getWorkerSettings(workerId);

        // Simulate failure
        if (random.nextDouble() < settings.getFailureRate()) {
            throw new Exception("Worker-" + workerId + " failed");
        }

        // Simulate latency with jitter (-10ms .. +10ms)
        int jitter = random.nextInt(21) - 10;
        int actualLatency = Math.max(0, settings.getLatency() + jitter);
        try {
            if (actualLatency > 0) {
                Thread.sleep(actualLatency);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        return "Worker-" + workerId + " processed: " + key;
    }

    /**
     * Get the worker count
     */
    public int getWorkerCount() {
        return workerConfig.getCount();
    }
}

