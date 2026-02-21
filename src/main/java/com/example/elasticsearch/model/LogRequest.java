package com.example.elasticsearch.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogRequest {

    @NotNull(message = "timestamp is required")
    private Instant timestamp;

    @NotBlank(message = "level is required")
    private String level;

    @NotBlank(message = "service is required")
    private String service;

    @NotBlank(message = "message is required")
    private String message;

    private String exception;
    private String traceId;
    private String host;
}
