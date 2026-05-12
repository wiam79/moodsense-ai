package com.moodsense.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodsense.api.exception.OpenAIException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SentimentAnalysisResult analyzeSentiment(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String systemPrompt = "You are an emotional intelligence AI. Analyze the sentiment of the text provided by the user. " +
                "Respond ONLY with a valid JSON object containing: sentiment (POSITIVE, NEGATIVE, or NEUTRAL), " +
                "confidenceScore (a number from 0 to 100 representing how confident you are), and " +
                "explanation (one short, calm sentence explaining your analysis). Do not include markdown, backticks, or any text outside the JSON object.";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", text)
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                return objectMapper.readValue(content, SentimentAnalysisResult.class);
            } else {
                log.error("OpenAI API error: Status Code {}", response.getStatusCode());
                throw new OpenAIException("Failed to get a valid response from AI service.");
            }
        } catch (Exception e) {
            log.error("Exception calling OpenAI API: {}", e.getMessage(), e);
            throw new OpenAIException("AI service is temporarily unavailable. Please try again.");
        }
    }

    @Data
    public static class SentimentAnalysisResult {
        private String sentiment;
        private Double confidenceScore;
        private String explanation;
    }
}
