package com.genixo.ges.api.common.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiErrorDto {

    Instant timestamp;
    int status;
    String error;
    String message;
    String path;
    String traceId;
    List<FieldViolationDto> fieldViolations;

    @Value
    @Builder
    public static class FieldViolationDto {
        String field;
        String message;
    }
}

