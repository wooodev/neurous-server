package com.example.server.domain.content.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Table(name = "content")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private int contentId;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(name = "content_category")
    private String contentCategory;

    @Column(name = "content_diff")
    private String contentDiff;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "news_article_id")
    private Long newsArticleId;
}