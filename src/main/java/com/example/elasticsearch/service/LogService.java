package com.example.elasticsearch.service;

import com.example.elasticsearch.model.LogDocument;
import com.example.elasticsearch.model.LogRequest;
import com.example.elasticsearch.model.PaginatedResponse;
import com.example.elasticsearch.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Service
public class LogService {

    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void save(LogRequest request) throws IOException {
        LogDocument document = LogDocument.fromRequest(request);
        String id = logRepository.index(document);
        log.debug("Saved log entry id={} service={} level={}", id, request.getService(), request.getLevel());
    }

    public PaginatedResponse<LogDocument> search(
            Optional<String> level,
            Optional<String> service,
            Optional<String> traceId,
            Optional<Instant> from,
            Optional<Instant> to,
            int page,
            int size
    ) throws IOException {
        LogRepository.SearchResult result = logRepository.search(level, service, traceId, from, to, page, size);
        return PaginatedResponse.<LogDocument>builder()
                .content(result.content())
                .page(result.page())
                .size(result.size())
                .totalElements(result.totalElements())
                .build();
    }
}
