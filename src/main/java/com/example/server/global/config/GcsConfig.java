package com.example.server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GcsConfig {

    @Bean
    public Storage gcsStorage() throws IOException {
        String path = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS is empty");
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(path));

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}