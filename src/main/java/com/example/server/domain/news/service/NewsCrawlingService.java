package com.example.server.domain.news.service;

import com.example.server.domain.news.client.NaverNewsSearchClient;
import com.example.server.domain.news.crawler.*;
import com.example.server.domain.news.dto.NaverNewsItem;
import com.example.server.domain.news.dto.NaverNewsResponse;
import com.example.server.domain.news.dto.NewsCrawlingProperties;
import com.example.server.domain.news.entity.NewsArticle;
import com.example.server.domain.news.entity.vo.NewsCategory;
import com.example.server.domain.news.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsCrawlingService {

    private final NaverNewsSearchClient naverNewsSearchClient;
    private final NewsArticleRepository newsArticleRepository;

    private final ArticleHtmlFetcher articleHtmlFetcher;
    private final ArticleParserRouter parserRouter;

    private final NewsCrawlingProperties props;

    @Value("${news.crawling.per-category-size:10}")
    private int perCategorySize;

    @Value("${news.crawling.request-delay-ms:200}")
    private long requestDelayMs;

    private static final DateTimeFormatter NAVER_PUBDATE = DateTimeFormatter.RFC_1123_DATE_TIME;

    @Transactional
    public int crawlAllCategories() {
        int saved = 0;
        for (NewsCategory category : NewsCategory.values()) {
            saved += crawlCategory(category);
            sleepQuietly(requestDelayMs);
        }
        return saved;
    }

    @Transactional
    public int crawlCategory(NewsCategory category) {

        String query = props.categoryQuery().get(category);

        if (query == null || query.isBlank()) {
            throw new IllegalStateException("카테고리 쿼리가 설정되어있지 않습니다: " + category);
        }

        NaverNewsResponse res = naverNewsSearchClient.searchNews(query, perCategorySize);

        if (res == null || res.items() == null || res.items().isEmpty()) return 0;

        int saved = 0;

        for (NaverNewsItem item : res.items()) {

            String sourceLink = chooseSourceLink(item);
            if (sourceLink.isBlank()) continue;

            if (newsArticleRepository.existsBySourceLink(sourceLink)) continue;

            String title = stripBold(safe(item.title()));
            String description = stripBold(safe(item.description()));
            String naverLink = safe(item.link());
            String originallink = safe(item.originallink());

            String html = articleHtmlFetcher.fetch(sourceLink);
            if (html == null || html.isBlank()) continue;

            ArticleParser parser = parserRouter.pick(sourceLink);
            ArticleParser.Parsed parsed = parser.parse(html, sourceLink);
            String raw = parsed == null ? "" : safe(parsed.raw());
            String clean = parsed == null ? "" : safe(parsed.clean());
            if (clean.isBlank()) continue;

            newsArticleRepository.save(new NewsArticle(
                    category,
                    title,
                    description,
                    sourceLink,
                    naverLink,
                    originallink,
                    raw,
                    clean,
                    parsePubDate(item.pubDate()),
                    OffsetDateTime.now()
            ));
            saved++;

            if (saved >= perCategorySize) break;
            sleepQuietly(requestDelayMs);

        }
        return saved;
    }

    private String chooseSourceLink(NaverNewsItem item) {
        String original = safe(item.originallink());
        if (original.startsWith("http://") || original.startsWith("https://")) return original;

        String link = safe(item.link());
        if (link.startsWith("http://") || link.startsWith("https://")) return link;

        return "";
    }

    private OffsetDateTime parsePubDate(String pubDate) {
        try {
            return ZonedDateTime.parse(pubDate, NAVER_PUBDATE).toOffsetDateTime();
        } catch (Exception e) {
            return OffsetDateTime.now();
        }
    }

    private String stripBold(String s) {
        return s.replace("<b>", "").replace("</b>", "");
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private void sleepQuietly(long ms) {
        if (ms <= 0) return;
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}