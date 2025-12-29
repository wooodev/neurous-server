package com.example.server.domain.news.entity;

import com.example.server.domain.news.entity.vo.NewsCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "news_article")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_article_id")
    private Long newsArticleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private NewsCategory category;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "source_link", length = 1200)
    private String sourceLink;

    @Column(name = "naver_link", length = 1200)
    private String naverLink;

    @Column(name = "originallink", length = 1200)
    private String originallink;

    @Column(name = "content_raw", columnDefinition = "TEXT")
    private String contentRaw;

    @Column(name = "content_clean", columnDefinition = "TEXT")
    private String contentClean;

    @Column(name = "published_at", nullable = false)
    private OffsetDateTime publishedAt;

    @Column(name = "crawled_at", nullable = false)
    private OffsetDateTime crawledAt;

    public NewsArticle(
            NewsCategory category,
            String title,
            String description,
            String sourceLink,
            String naverLink,
            String originallink,
            String contentRaw,
            String contentClean,
            OffsetDateTime publishedAt,
            OffsetDateTime crawledAt
    ) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.sourceLink = sourceLink;
        this.naverLink = naverLink;
        this.originallink = originallink;
        this.contentRaw = contentRaw;
        this.contentClean = contentClean;
        this.publishedAt = publishedAt;
        this.crawledAt = crawledAt;
    }
}