package com.example.server.domain.news.client;

import org.springframework.context.annotation.Profile;

@Profile("!test")
public interface ObjectStorageClient {
    boolean exists(String key);

    String uploadPng(String key, byte[] pngBytes);

    String buildKey(String filename);

    String buildPublicUrl(String key);
}