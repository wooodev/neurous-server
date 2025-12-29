package com.example.server.domain.quiz.repository;

import com.example.server.domain.quiz.entity.QuizSolve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizSolveRepository extends JpaRepository<QuizSolve, Long> {
    Optional<QuizSolve> findByUserIdAndContentId(Long userId, int contentId);
}