package com.example.controller;

import com.example.vo.TestResult;
import com.example.service.ConsistentHashService;
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
    private ConsistentHashService consistentHashService;

    @GetMapping("/heterogeneous-nodes")
    public ResponseEntity<?> heterogeneousNodes() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/hot-key")
    public ResponseEntity<?> hotKey() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/partial-failure")
    public ResponseEntity<?> partialFailure() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}


