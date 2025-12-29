package com.example.server.domain.news.crawler;

public interface ArticleParser {
    boolean supports(String url);
    Parsed parse(String html, String url);

    record Parsed(String raw, String clean) {}
}