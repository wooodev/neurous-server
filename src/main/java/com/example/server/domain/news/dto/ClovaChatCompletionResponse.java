package com.example.server.domain.news.dto;

import java.util.List;

public record ClovaChatCompletionResponse(
        List<Choice> choices
) {
    public String contentOrEmpty() {
        if (choices == null || choices.isEmpty()) return "";
        Choice c = choices.get(0);
        if (c == null || c.message() == null || c.message().content() == null) return "";
        return c.message().content();
    }

    public record Choice(Message message) {}
    public record Message(String role, String content) {}
}