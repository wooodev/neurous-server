package com.example.server.domain.news.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gcs.object-storage")
public record GcsObjectStorageProperties(
        String bucket,
        String prefix,
        String publicBaseUrl
) {}
