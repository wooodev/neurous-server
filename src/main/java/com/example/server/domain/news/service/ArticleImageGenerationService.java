package com.example.server.domain.news.service;

import com.example.server.domain.news.client.GeminiImageClient;
import com.example.server.domain.news.client.ObjectStorageClient;
import com.example.server.domain.news.dto.GeminiImageProperties;
import com.example.server.domain.news.entity.NewsArticle;
import com.example.server.domain.news.repository.NewsArticleRepository;
import com.example.server.global.exception.model.GeminiNoImageException;
import com.example.server.global.exception.model.GeminiRateLimitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test")
public class ArticleImageGenerationService {

    private final GeminiImageClient gemini;
    private final GeminiImageProperties geminiProps;

    private final ObjectStorageClient objectStorage;
    private final NewsArticleRepository newsArticleRepository;

    private final Semaphore singleFlight = new Semaphore(1);
    private final AtomicLong nextAllowedAtMs = new AtomicLong(0);

    @Transactional
    public String ensureImageUrl(NewsArticle a) {

        if (a.getImageUrl() != null && !a.getImageUrl().isBlank()) {
            return a.getImageUrl();
        }

        String prompt = buildImagePrompt(a);

        try {
            singleFlight.acquire();
            throttleByMinInterval();

            byte[] png = callWithRetry(a.getNewsArticleId(), prompt);
            if (png == null) return null;

            String key = objectStorage.buildKey("news_" + a.getNewsArticleId() + ".png");
            String url = objectStorage.uploadPng(key, png);

            a.setImageUrl(url);
            newsArticleRepository.save(a);

            return url;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted while generating image", e);
        } finally {
            singleFlight.release();
        }
    }

    private void throttleByMinInterval() {
        int rpm = Math.max(geminiProps.maxRpm(), 1);
        long minIntervalMs = Math.max(60_000L / rpm, 200L);

        while (true) {
            long now = System.currentTimeMillis();
            long allowedAt = nextAllowedAtMs.get();
            long wait = allowedAt - now;

            if (wait <= 0) {
                if (nextAllowedAtMs.compareAndSet(allowedAt, now + minIntervalMs)) return;
                continue;
            }
            sleepQuietly(wait);
        }
    }

    private byte[] callWithRetry(Long newsArticleId, String prompt) {
        RuntimeException last = null;

        int maxTry = Math.max(geminiProps.maxRetries(), 0) + 1;

        for (int attempt = 1; attempt <= maxTry; attempt++) {
            try {
                return gemini.generatePng(prompt);

            } catch (GeminiRateLimitException e) {
                last = e;
                if (attempt == maxTry) break;

                long backoff = computeBackoffMs(attempt);
                sleepQuietly(backoff);

            } catch (GeminiNoImageException e) {
                return null;
            }
        }
        return null;
    }

    private long computeBackoffMs(int attempt) {
        long base = Math.max(geminiProps.baseBackoffMs(), 200);
        long max = Math.max(geminiProps.maxBackoffMs(), base);

        long exp = base * (1L << Math.min(attempt - 1, 10));
        long capped = Math.min(exp, max);

        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(250, capped / 4));
        return Math.min(capped + jitter, max);
    }

    private void sleepQuietly(long ms) {
        if (ms <= 0) return;
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    private String buildImagePrompt(NewsArticle a) {
        String base = firstNonBlank(a.getContentClean(), a.getDescription(), a.getTitle());
        String title = firstNonBlank(a.getTitle(), "");

        return """
            Create a 16:9 thumbnail illustration for a news article.

            Requirements:
            - No text, no letters, no captions, no watermarks, no logos.
            - No borders, no UI elements, no corner labels.
            - A single clear focal subject, high contrast, clean composition.

            Article title: %s
            Article context: %s
            """.formatted(title, base);
    }

    private String firstNonBlank(String... xs) {
        for (String x : xs) {
            if (x != null && !x.isBlank()) return x;
        }
        return "";
    }
}
