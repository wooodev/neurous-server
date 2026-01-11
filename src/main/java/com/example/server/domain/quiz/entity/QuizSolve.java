package com.example.server.domain.quiz.entity;

import java.time.LocalDateTime;

import com.example.server.domain.content.entity.ReadContent;
import com.example.server.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_solve")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizSolve {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "quiz_solve_id")
	private Long quizSolveId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "read_content_id")
	private ReadContent readContent;

	@Column(name = "quiz_id", nullable = false)
	private Long quizId;

	@Column(name = "selected_no", nullable = false)
	private int selectedNo;

	@Column(nullable = false, name = "is_answer_correct", columnDefinition = "TINYINT(1)")
	private boolean isAnswerCorrect;

	@Column(name = "solved_at", nullable = false)
	private LocalDateTime solvedAt;

	private QuizSolve(User user, ReadContent readContent, Long quizId, int selectedNo, boolean isAnswerCorrect,
		LocalDateTime solvedAt) {
		this.user = user;
		this.readContent = readContent;
		this.quizId = quizId;
		this.selectedNo = selectedNo;
		this.isAnswerCorrect = isAnswerCorrect;
		this.solvedAt = solvedAt;
	}

	public static QuizSolve of(User user, ReadContent readContent, Long quizId, int selectedNo, boolean isAnswerCorrect,
		LocalDateTime solvedAt) {
		return new QuizSolve(user, readContent, quizId, selectedNo, isAnswerCorrect, solvedAt);
	}
}
