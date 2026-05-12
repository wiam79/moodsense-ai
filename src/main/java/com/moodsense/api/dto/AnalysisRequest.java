package com.moodsense.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnalysisRequest {
    @NotBlank(message = "Text input is required")
    @Size(max = 500, message = "Text input must not exceed 500 characters")
    private String text;
}
