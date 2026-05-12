package com.moodsense.api.service;

import com.moodsense.api.dto.AnalysisRequest;
import com.moodsense.api.dto.AnalysisResponse;
import com.moodsense.api.model.Analysis;
import com.moodsense.api.model.User;
import com.moodsense.api.repository.AnalysisRepository;
import com.moodsense.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final OpenAIService openAIService;

    // In-memory rate limiting: Map of UserId to a Map of Timestamp (Hour) to Count
    private final Map<Long, Map<Long, Integer>> userRequestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_HOUR = 20;

    public AnalysisResponse analyzeText(Long userId, AnalysisRequest request) {
        checkRateLimit(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String text = request.getText().trim();

        // Call OpenAI
        OpenAIService.SentimentAnalysisResult aiResult = openAIService.analyzeSentiment(text);

        Analysis analysis = Analysis.builder()
                .textInput(text)
                .sentiment(aiResult.getSentiment())
                .confidenceScore(aiResult.getConfidenceScore())
                .aiExplanation(aiResult.getExplanation())
                .user(user)
                .build();

        Analysis savedAnalysis = analysisRepository.save(analysis);
        log.info("Analysis created with id: {} for user: {}", savedAnalysis.getId(), userId);

        return mapToResponse(savedAnalysis);
    }

    public List<AnalysisResponse> getHistory(Long userId) {
        List<Analysis> analyses = analysisRepository.findAllByUserIdOrderByAnalyzedAtDesc(userId);
        return analyses.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    public AnalysisResponse getAnalysis(Long id, Long userId) {
        Analysis analysis = analysisRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AccessDeniedException("Analysis not found or access denied"));
        return mapToResponse(analysis);
    }

    public void deleteAnalysis(Long id, Long userId) {
        Analysis analysis = analysisRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AccessDeniedException("Analysis not found or access denied"));
        analysisRepository.delete(analysis);
        log.info("Analysis {} deleted by user {}", id, userId);
    }

    public Map<String, Object> getStats(Long userId) {
        long total = analysisRepository.countByUserId(userId);
        long positive = analysisRepository.countByUserIdAndSentiment(userId, "POSITIVE");
        long negative = analysisRepository.countByUserIdAndSentiment(userId, "NEGATIVE");
        long neutral = analysisRepository.countByUserIdAndSentiment(userId, "NEUTRAL");
        
        Analysis latest = analysisRepository.findFirstByUserIdOrderByAnalyzedAtDesc(userId).orElse(null);

        return Map.of(
                "totalAnalyses", total,
                "positiveCount", positive,
                "negativeCount", negative,
                "neutralCount", neutral,
                "lastAnalyzedAt", latest != null ? latest.getAnalyzedAt() : "Never"
        );
    }

    private void checkRateLimit(Long userId) {
        long currentHour = System.currentTimeMillis() / (1000 * 60 * 60);
        
        userRequestCounts.putIfAbsent(userId, new ConcurrentHashMap<>());
        Map<Long, Integer> counts = userRequestCounts.get(userId);
        
        counts.keySet().removeIf(hour -> hour < currentHour);

        int count = counts.getOrDefault(currentHour, 0);
        if (count >= MAX_REQUESTS_PER_HOUR) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded. Try again later.");
        }
        
        counts.put(currentHour, count + 1);
    }

    private AnalysisResponse mapToResponse(Analysis analysis) {
        return AnalysisResponse.builder()
                .id(analysis.getId())
                .sentiment(analysis.getSentiment())
                .confidenceScore(analysis.getConfidenceScore())
                .aiExplanation(analysis.getAiExplanation())
                .analyzedAt(analysis.getAnalyzedAt())
                .textInput(analysis.getTextInput())
                .build();
    }
    
    private AnalysisResponse mapToHistoryResponse(Analysis analysis) {
        String truncatedText = analysis.getTextInput();
        if (truncatedText != null && truncatedText.length() > 100) {
            truncatedText = truncatedText.substring(0, 100) + "...";
        }
        
        return AnalysisResponse.builder()
                .id(analysis.getId())
                .sentiment(analysis.getSentiment())
                .confidenceScore(analysis.getConfidenceScore())
                .aiExplanation(analysis.getAiExplanation())
                .analyzedAt(analysis.getAnalyzedAt())
                .textInput(truncatedText)
                .build();
    }
}
