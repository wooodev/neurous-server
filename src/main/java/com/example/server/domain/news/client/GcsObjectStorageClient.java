package com.example.server.domain.news.client;

import com.example.server.domain.news.dto.GcsObjectStorageProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "gcs.object-storage.provider", havingValue = "gcs")
@Profile("!test")
public class GcsObjectStorageClient implements ObjectStorageClient {

    private final Storage storage;
    private final GcsObjectStorageProperties props;

    @Override
    public boolean exists(String key) {
        return storage.get(props.bucket(), key) != null;
    }

    @Override
    public String uploadPng(String key, byte[] pngBytes) {

        try {
            GoogleCredentials creds = GoogleCredentials.getApplicationDefault();
            log.info("[GCS] credentialClass={}", creds.getClass().getName());
        } catch (Exception e) {
            log.warn("[GCS] failed to get ADC credential class. msg={}", e.getMessage());
        }

        BlobInfo blobInfo = BlobInfo.newBuilder(props.bucket(), key)
                .setContentType("image/png")
                .build();

        storage.create(blobInfo, pngBytes);

        return buildPublicUrl(key);
    }

    @Override
    public String buildKey(String filename) {
        String p = (props.prefix() == null || props.prefix().isBlank()) ? "" : props.prefix().trim();
        if (p.isEmpty()) return filename;
        if (p.endsWith("/")) return p + filename;
        return p + "/" + filename;
    }

    @Override
    public String buildPublicUrl(String key) {
        String base = (props.publicBaseUrl() != null && !props.publicBaseUrl().isBlank())
                ? props.publicBaseUrl().trim()
                : ("https://storage.googleapis.com/" + props.bucket());

        String safeKey = key.replace(" ", "%20");
        return base + "/" + safeKey;
    }
}