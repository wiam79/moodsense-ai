package com.moodsense.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnalysisResponse {
    private Long id;
    private String sentiment;
    private Double confidenceScore;
    private String aiExplanation;
    private LocalDateTime analyzedAt;
    private String textInput;
}
