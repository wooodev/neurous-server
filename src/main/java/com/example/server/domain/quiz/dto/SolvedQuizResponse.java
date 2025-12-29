package com.example.server.domain.quiz.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record SolvedQuizResponse(
        int quizId,
        int contentId,
        String quizDiff,
        String quizCategory,
        String quizContent,
        List<QuizChoiceResponse> choices,
        int selectedNo,
        int correctChoiceNo,
        boolean correct,
        LocalDateTime solvedAt
) {
    public static SolvedQuizResponse of(
            int quizId,
            int contentId,
            String quizContent,
            List<QuizChoiceResponse> choices,
            int correctChoiceNo,
            boolean correct
    ) {
        return SolvedQuizResponse.builder()
                .quizId(quizId)
                .contentId(contentId)
                .quizContent(quizContent)
                .choices(choices)
                .correctChoiceNo(correctChoiceNo)
                .correct(correct)
                .build();
    }
}
