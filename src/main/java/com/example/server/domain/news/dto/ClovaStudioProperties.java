package com.example.server.domain.news.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clova")
public record ClovaStudioProperties(
        String baseUrl,
        String model,
        String apiKey,
        long timeoutMs
) {
}