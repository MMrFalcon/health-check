package com.falcon.apps.health.service;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Service
public class HealthCheckService {
    private final WebClient webClient;
    public HealthCheckService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://td-p.pl").build();
    }

    @Scheduled(fixedRate = 5000)
    public Mono<HttpStatusCode> checkHealth() {
        return this.webClient.get().uri("/").accept(MediaType.TEXT_HTML).retrieve().toEntity(String.class)
                .map(ResponseEntity::getStatusCode).log();
    }


}
