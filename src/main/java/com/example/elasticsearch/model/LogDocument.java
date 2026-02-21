package com.example.elasticsearch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogDocument {

    private Instant timestamp;
    private String level;
    private String service;
    private String message;
    private String exception;
    private String traceId;
    private String host;

    public static LogDocument fromRequest(LogRequest request) {
        return LogDocument.builder()
                .timestamp(request.getTimestamp())
                .level(request.getLevel())
                .service(request.getService())
                .message(request.getMessage())
                .exception(request.getException())
                .traceId(request.getTraceId())
                .host(request.getHost())
                .build();
    }
}
