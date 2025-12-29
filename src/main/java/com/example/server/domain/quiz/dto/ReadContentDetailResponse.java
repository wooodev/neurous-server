package com.example.server.domain.quiz.dto;

import com.example.server.domain.content.dto.ContentResponse;
import lombok.Builder;

@Builder
public record ReadContentDetailResponse(
        ContentResponse content,
        SolvedQuizResponse quiz
) {
    public static ReadContentDetailResponse of(ContentResponse content, SolvedQuizResponse quiz) {
        return ReadContentDetailResponse.builder()
                .content(content)
                .quiz(quiz)
                .build();
    }
}