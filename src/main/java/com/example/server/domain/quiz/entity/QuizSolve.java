package com.example.server.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_solve")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizSolve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_solve_id")
    private Long quizSolveId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "content_id")
    private int contentId;

    @Column(name = "quiz_id")
    private int quizId;

    @Column(name = "selected_no")
    private int selectedNo;

    @Column(name = "is_answer_correct")
    private boolean isAnswerCorrect;

    @Column(name = "solved_at")
    private LocalDateTime solvedAt;

    private QuizSolve(Long userId, int contentId, int quizId, int selectedNo, boolean isAnswerCorrect, LocalDateTime solvedAt) {
        this.userId = userId;
        this.contentId = contentId;
        this.quizId = quizId;
        this.selectedNo = selectedNo;
        this.isAnswerCorrect = isAnswerCorrect;
        this.solvedAt = solvedAt;
    }

    public static QuizSolve of(Long userId, int contentId, int quizId, int selectedNo, boolean isAnswerCorrect, LocalDateTime solvedAt) {
        return new QuizSolve(userId, contentId, quizId, selectedNo, isAnswerCorrect, solvedAt);
    }
}