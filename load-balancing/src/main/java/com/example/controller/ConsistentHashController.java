package com.example.controller;

import com.example.vo.TestResult;
import com.example.service.impl.ConsistentHashServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/consistent-hash")
public class ConsistentHashController {

    @Autowired
    private ConsistentHashServiceImpl consistentHashServiceImpl;

    @GetMapping("/heterogeneous-nodes")
    public ResponseEntity<?> heterogeneousNodes() {
        try {
            TestResult result = consistentHashServiceImpl.runHeterogeneousNodes();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/hot-key")
    public ResponseEntity<?> hotKey() {
        try {
            TestResult result = consistentHashServiceImpl.runHotKey();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/partial-failure")
    public ResponseEntity<?> partialFailure() {
        try {
            TestResult result = consistentHashServiceImpl.runPartialFailure();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}