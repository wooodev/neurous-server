package com.example.server.domain.quiz.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.server.domain.content.entity.vo.ContentLevel;
import com.example.server.domain.quiz.entity.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

	@Query(" SELECT q FROM Quiz q WHERE q.content.contentId = :contentId AND q.quizDiff = :quizDiff")
	Optional<Quiz> findQuiz(@Param("contentId") Long contentId, @Param("quizDiff") ContentLevel quizDiff);

	Optional<Quiz> findByContent_ContentIdAndQuizDiff(Long contentId, String quizDiff);

	boolean existsByContent_ContentId(Long contentId);

	@Query("""
        select coalesce(max(q.quizNum), 0)
        from Quiz q
        where q.content.contentId = :contentId
    """)
	int findMaxQuizNumByContentId(@Param("contentId") Long contentId);

	@Query("select q from Quiz q join fetch q.content where q.quizId = :quizId")
	Optional<Quiz> findByIdWithContent(@Param("quizId") Long quizId);
}
