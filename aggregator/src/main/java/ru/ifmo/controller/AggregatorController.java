package ru.ifmo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.dto.AggregatedResult;
import ru.ifmo.service.AggregationService;
import ru.ifmo.service.ResultStorageService;

import java.util.List;

@RestController
@RequestMapping("/api/aggregator")
@RequiredArgsConstructor
public class AggregatorController {
    
    private final AggregationService aggregationService;
    private final ResultStorageService resultStorageService;
    
    @GetMapping("/sessions")
    public ResponseEntity<List<String>> getActiveSessions() {
        List<String> sessions = aggregationService.getActiveSessions();
        return ResponseEntity.ok(sessions);
    }
    
    @GetMapping("/sessions/{sessionId}/count")
    public ResponseEntity<Integer> getResultCount(@PathVariable String sessionId) {
        int count = aggregationService.getResultCount(sessionId);
        return ResponseEntity.ok(count);
    }
    
    @PostMapping("/sessions/{sessionId}/expected/{expectedCount}")
    public ResponseEntity<Void> setExpectedTaskCount(
            @PathVariable String sessionId,
            @PathVariable int expectedCount) {
        aggregationService.setExpectedTaskCount(sessionId, expectedCount);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/sessions/{sessionId}/ready")
    public ResponseEntity<Boolean> isSessionReady(@PathVariable String sessionId) {
        boolean ready = aggregationService.isReadyForAggregation(sessionId);
        return ResponseEntity.ok(ready);
    }
    
    @PostMapping("/sessions/{sessionId}/aggregate")
    public ResponseEntity<AggregatedResult> triggerAggregation(@PathVariable String sessionId) {
        AggregatedResult result = aggregationService.aggregateResults(sessionId);
        if (result != null) {
            resultStorageService.storeResult(result);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> clearSession(@PathVariable String sessionId) {
        aggregationService.clearSession(sessionId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/results")
    public ResponseEntity<List<String>> getStoredResultIds() {
        List<String> resultIds = resultStorageService.getStoredResultIds();
        return ResponseEntity.ok(resultIds);
    }
    
    @GetMapping("/results/{aggregationId}")
    public ResponseEntity<AggregatedResult> getResult(@PathVariable String aggregationId) {
        AggregatedResult result = resultStorageService.getResult(aggregationId);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/results/all")
    public ResponseEntity<List<AggregatedResult>> getAllResults() {
        List<AggregatedResult> results = resultStorageService.getAllResults();
        return ResponseEntity.ok(results);
    }
    
    @DeleteMapping("/results/{aggregationId}")
    public ResponseEntity<Void> deleteResult(@PathVariable String aggregationId) {
        boolean deleted = resultStorageService.deleteResult(aggregationId);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/results")
    public ResponseEntity<Void> clearAllResults() {
        resultStorageService.clearAllResults();
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Aggregator is running");
    }
}