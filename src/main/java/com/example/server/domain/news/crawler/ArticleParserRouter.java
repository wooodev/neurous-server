package com.example.server.domain.news.crawler;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ArticleParserRouter {

    private final List<ArticleParser> parsers;

    public ArticleParserRouter(List<ArticleParser> parsers) {
        this.parsers = parsers;
    }

    public ArticleParser pick(String url) {
        return parsers.stream()
                .filter(p -> p.supports(url))
                .findFirst()
                .orElseThrow();
    }
}