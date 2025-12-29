package com.example.server.domain.quiz.dto;

import com.example.server.domain.quiz.entity.QuizChoice;
import lombok.Builder;

@Builder
public record QuizSubmitResponse(
        int quizId,
        int selectedNo,
        Boolean isAnswerCorrect,
        int correctChoiceId,
        int correctChoiceNo,
        String correctChoiceText
) {
    public static QuizSubmitResponse of(int quizId, int selectedNo, boolean isAnswerCorrect, QuizChoice correctChoice) {
        return QuizSubmitResponse.builder()
                .quizId(quizId)
                .selectedNo(selectedNo)
                .isAnswerCorrect(isAnswerCorrect)
                .correctChoiceNo(correctChoice.getChoiceNo())
                .correctChoiceText(correctChoice.getChoiceText())
                .build();
    }
}
