package com.moodsense.api.controller;

import com.moodsense.api.dto.AnalysisRequest;
import com.moodsense.api.dto.AnalysisResponse;
import com.moodsense.api.security.CustomUserDetails;
import com.moodsense.api.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyze(Authentication authentication, @Valid @RequestBody AnalysisRequest request) {
        Long userId = getUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(analysisService.analyzeText(userId, request));
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<AnalysisResponse> history = analysisService.getHistory(userId);
        return ResponseEntity.ok(Map.of("data", history, "count", history.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResponse> getAnalysis(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(analysisService.getAnalysis(id, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysis(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserId(authentication);
        analysisService.deleteAnalysis(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(analysisService.getStats(userId));
    }

    private Long getUserId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}
