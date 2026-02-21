package com.example.elasticsearch.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.DateRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.example.elasticsearch.model.LogDocument;
import com.example.elasticsearch.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class LogRepository {

    private static final Logger log = LoggerFactory.getLogger(LogRepository.class);

    private final ElasticsearchClient client;
    private final String indexName;

    public LogRepository(ElasticsearchClient client, AppProperties appProperties) {
        this.client = client;
        this.indexName = Objects.requireNonNull(
                appProperties.getIndexName(),
                "LOG_INDEX_NAME is required"
        );
    }

    public void createIndexIfNotExists() throws IOException {
        ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(indexName));
        if (client.indices().exists(existsRequest).value()) {
            log.debug("Index {} already exists", indexName);
            return;
        }

        TypeMapping mapping = TypeMapping.of(m -> m
                .properties("timestamp", Property.of(p -> p.date(d -> d.format("strict_date_optional_time||epoch_millis"))))
                .properties("level", Property.of(p -> p.keyword(k -> k)))
                .properties("service", Property.of(p -> p.keyword(k -> k)))
                .properties("message", Property.of(p -> p.text(t -> t)))
                .properties("traceId", Property.of(p -> p.keyword(k -> k)))
                .properties("host", Property.of(p -> p.keyword(k -> k)))
                .properties("exception", Property.of(p -> p.text(t -> t)))
        );

        CreateIndexRequest createRequest = CreateIndexRequest.of(c -> c
                .index(indexName)
                .mappings(mapping)
        );

        client.indices().create(createRequest);
        log.info("Created index {} with mappings", indexName);
    }

    public String index(LogDocument document) throws IOException {
        createIndexIfNotExists();

        IndexRequest<LogDocument> request = IndexRequest.of(i -> i
                .index(indexName)
                .document(document)
        );

        IndexResponse response = client.index(request);
        log.debug("Indexed log document, id={}", response.id());
        return response.id();
    }

    public SearchResult search(
            Optional<String> level,
            Optional<String> service,
            Optional<String> traceId,
            Optional<Instant> from,
            Optional<Instant> to,
            int page,
            int size
    ) throws IOException {
        List<Query> must = new ArrayList<>();

        level.ifPresent(l -> must.add(Query.of(q -> q.term(TermQuery.of(t -> t.field("level").value(l))))));
        service.ifPresent(s -> must.add(Query.of(q -> q.term(TermQuery.of(t -> t.field("service").value(s))))));
        traceId.ifPresent(tid -> must.add(Query.of(q -> q.term(TermQuery.of(t -> t.field("traceId").value(tid))))));

        if (from.isPresent() || to.isPresent()) {
            DateRangeQuery dateRange = DateRangeQuery.of(d -> {
                d.field("timestamp");
                from.ifPresent(f -> d.gte(f.toString()));
                to.ifPresent(t -> d.lte(t.toString()));
                return d;
            });
            must.add(Query.of(q -> q.range(RangeQuery.of(r -> r.date(dateRange)))));
        }

        Query boolQuery = must.isEmpty()
                ? Query.of(q -> q.matchAll(m -> m))
                : Query.of(q -> q.bool(BoolQuery.of(b -> b.must(must))));

        int fromOffset = page * size;

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .query(boolQuery)
                .from(fromOffset)
                .size(size)
                .sort(so -> so.field(f -> f.field("timestamp").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
        );

        SearchResponse<LogDocument> response = client.search(searchRequest, LogDocument.class);
        long total = response.hits().total() != null ? response.hits().total().value() : 0;
        List<LogDocument> content = response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();

        return new SearchResult(content, page, size, total);
    }

    public record SearchResult(List<LogDocument> content, int page, int size, long totalElements) {}
}
