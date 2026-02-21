package com.example.elasticsearch.controller;

import com.example.elasticsearch.model.LogDocument;
import com.example.elasticsearch.model.LogRequest;
import com.example.elasticsearch.model.PaginatedResponse;
import com.example.elasticsearch.service.LogService;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("${logserver.endpoint-path:/api/logs}")
public class LogController {

    private static final Logger log = LoggerFactory.getLogger(LogController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 50;

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Nonnull
    public ResponseEntity<Void> createLog(@Valid @RequestBody LogRequest request) throws IOException {
        log.debug("POST log: service={} level={}", request.getService(), request.getLevel());
        logService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Nonnull
    public ResponseEntity<PaginatedResponse<LogDocument>> getLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size
    ) throws IOException {
        Optional<Instant> fromInstant = parseOptionalInstant(from);
        Optional<Instant> toInstant = parseOptionalInstant(to);
        int pageVal = page >= 0 ? page : DEFAULT_PAGE;
        int sizeVal = size > 0 && size <= 1000 ? size : DEFAULT_SIZE;

        PaginatedResponse<LogDocument> response = logService.search(
                Optional.ofNullable(level).filter(s -> !s.isBlank()),
                Optional.ofNullable(service).filter(s -> !s.isBlank()),
                Optional.ofNullable(traceId).filter(s -> !s.isBlank()),
                fromInstant,
                toInstant,
                pageVal,
                sizeVal
        );
        return ResponseEntity.ok(response);
    }

    private static Optional<Instant> parseOptionalInstant(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
