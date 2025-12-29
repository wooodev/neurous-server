package com.example.server.domain.news.dto;

import com.example.server.domain.news.entity.vo.NewsCategory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.EnumMap;
import java.util.Map;

@ConfigurationProperties(prefix = "news.crawling")
public record NewsCrawlingProperties(
        Map<NewsCategory, String> categoryQuery
) {
    public Map<NewsCategory, String> categoryQueryOrEmpty() {
        return categoryQuery == null ? new EnumMap<>(NewsCategory.class) : categoryQuery;
    }
}