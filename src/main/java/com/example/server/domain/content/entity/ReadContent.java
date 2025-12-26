package com.example.server.domain.content.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "read_content")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReadContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "read_content_id")
    private int readContentId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "content_id")
    private int contentId;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    private ReadContent(Long userId, Integer contentId, LocalDateTime readAt) {
        this.userId = userId;
        this.contentId = contentId;
        this.readAt = readAt;
    }

    public static ReadContent of(Long userId, Integer contentId, LocalDateTime now) {
        return new ReadContent(userId, contentId, now);
    }

    public void setTime(LocalDateTime now) {
        this.readAt = now;
    }
}
