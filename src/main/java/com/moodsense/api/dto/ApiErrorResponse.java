package com.moodsense.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorResponse {
    private int status;
    private String error;
    private Object message;
    private String timestamp;
}
