package com.example.server.domain.news.client;

import com.example.server.domain.news.dto.ClovaChatCompletionRequest;
import com.example.server.domain.news.dto.ClovaChatCompletionResponse;
import com.example.server.domain.news.dto.ClovaStudioProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class ClovaStudioClient {

    private final WebClient webClient;
    private final ClovaStudioProperties props;

    public ClovaStudioClient(WebClient.Builder builder, ClovaStudioProperties props) {
        this.props = props;
        this.webClient = builder
                .baseUrl(props.baseUrl())
                .build();
    }

    public String chat(ClovaChatCompletionRequest request) {
        try {
            ClovaChatCompletionResponse res = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey())
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            resp -> resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "[CLOVA] HTTP " + resp.statusCode().value() + " body=" + body
                                    )))
                    )
                    .bodyToMono(ClovaChatCompletionResponse.class)
                    .timeout(Duration.ofMillis(props.timeoutMs()))
                    .block();

            return (res == null) ? "" : res.contentOrEmpty().trim();

        } catch (WebClientResponseException e) {
            String body = e.getResponseBodyAsString();
            throw new RuntimeException("[CLOVA] HTTP " + e.getStatusCode().value() + " body=" + body, e);
        }
    }
}