package com.example.server.domain.news.client;

import com.example.server.domain.news.dto.NaverNewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class NaverNewsSearchClient {

    private final WebClient webClient;

    @Value("${naver.openapi.client-id}")
    private String clientId;

    @Value("${naver.openapi.client-secret}")
    private String clientSecret;

    @Value("${naver.openapi.sort:date}")
    private String sort;


    public NaverNewsResponse searchNews(String query, int display) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("openapi.naver.com")
                        .path("/v1/search/news.json")
                        .queryParam("query", query)
                        .queryParam("display", display)
                        .queryParam("start", 1)
                        .queryParam("sort", sort)
                        .build())
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .bodyToMono(NaverNewsResponse.class)
                .block();
    }
}