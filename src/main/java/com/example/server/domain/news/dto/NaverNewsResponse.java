package com.example.server.domain.news.dto;

import java.util.List;

public record NaverNewsResponse(
        int total,
        int start,
        int display,
        List<NaverNewsItem> items
) {
}
