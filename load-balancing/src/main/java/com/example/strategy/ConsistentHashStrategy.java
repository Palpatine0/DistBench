package com.example.strategy;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class ConsistentHashStrategy implements LoadBalancerStrategy {

    @Override
    public int selectWorker(String key, int totalWorkers) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private int hashKey(String key) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

