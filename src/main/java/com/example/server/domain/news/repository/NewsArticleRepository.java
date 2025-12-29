package com.example.server.domain.news.repository;

import com.example.server.domain.news.entity.NewsArticle;
import com.example.server.domain.news.entity.vo.NewsCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;


public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    boolean existsBySourceLink(String sourceLink);
    Page<NewsArticle> findByCategoryOrderByPublishedAtDesc(NewsCategory category, Pageable pageable);

    List<NewsArticle> findByCategoryAndCrawledAtBetweenOrderByCrawledAtAsc(
            NewsCategory category,
            OffsetDateTime from,
            OffsetDateTime to
    );
}