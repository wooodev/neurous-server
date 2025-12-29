package com.example.server.domain.news.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import javax.net.ssl.SSLHandshakeException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleHtmlFetcher {

    private final WebClient crawlingWebClient;

    public String fetch(String url) {
        try {
            return crawlingWebClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientRequestException e) {

            Throwable root = rootCause(e);

            if (root instanceof SSLHandshakeException
                    || (root.getMessage() != null && root.getMessage().contains("PKIX path building failed"))) {
                log.warn("[FETCH] SSL handshake failed. skip url={}", url);
                return null;
            }

            log.warn("[FETCH] Request failed. skip url={} cause={}", url, root.toString());
            return null;

        } catch (Exception e) {
            log.warn("[FETCH] Unknown error. skip url={} cause={}", url, e.toString());
            return null;
        }
    }

    private Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }
}