package com.example.elasticsearch;

import com.example.elasticsearch.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ELoggerApplication {

	private static final Logger log = LoggerFactory.getLogger(ELoggerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ELoggerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady(ApplicationReadyEvent event) {
		String kibanaUrl = event.getApplicationContext().getBean(AppProperties.class).getKibanaUrl();
		if (kibanaUrl != null) {
			log.info("Elasticsearch log viewer (Kibana): {}", kibanaUrl);
		}
	}
}
