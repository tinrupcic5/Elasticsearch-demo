package com.example.elasticsearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "logserver")
public class AppProperties {

    private String endpointPath;

    private ElasticsearchProperties elasticsearch = new ElasticsearchProperties();

    private String indexName;

    private String kibanaUrl;

    @Data
    public static class ElasticsearchProperties {
        private String url;
    }
}
