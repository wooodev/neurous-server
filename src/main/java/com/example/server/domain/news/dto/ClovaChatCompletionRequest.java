package com.example.server.domain.news.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClovaChatCompletionRequest(
        String model,
        List<Message> messages,
        Double temperature,
        @JsonProperty("max_tokens") Integer maxTokens
) {
    public record Message(String role, String content) {}
}