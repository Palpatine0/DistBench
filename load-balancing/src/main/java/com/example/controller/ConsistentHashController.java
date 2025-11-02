package com.example.controller;

import com.example.service.impl.ConsistentHashServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consistent-hash")
public class ConsistentHashController {

    @Autowired
    private ConsistentHashServiceImpl consistentHashServiceImpl;

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


