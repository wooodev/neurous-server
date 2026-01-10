package com.example.server.domain.quiz.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.server.domain.content.entity.ReadContent;
import com.example.server.domain.content.entity.vo.ContentLevel;
import com.example.server.domain.content.repository.ReadContentRepository;
import com.example.server.domain.mission.entity.vo.MissionType;
import com.example.server.domain.quiz.dto.request.QuizSubmitRequest;
import com.example.server.domain.quiz.dto.response.QuizChoiceResponse;
import com.example.server.domain.quiz.dto.response.QuizQuestionResponse;
import com.example.server.domain.quiz.dto.response.QuizResultResponse;
import com.example.server.domain.quiz.dto.response.QuizSubmitResponse;
import com.example.server.domain.quiz.entity.Quiz;
import com.example.server.domain.quiz.entity.QuizChoice;
import com.example.server.domain.quiz.entity.QuizSolve;
import com.example.server.domain.quiz.repository.QuizChoiceRepository;
import com.example.server.domain.quiz.repository.QuizRepository;
import com.example.server.domain.quiz.repository.QuizSolveRepository;
import com.example.server.domain.reward.dto.response.LevelUpInfo;
import com.example.server.domain.reward.dto.response.RewardResponse;
import com.example.server.domain.reward.entity.RewardHistory;
import com.example.server.domain.reward.entity.vo.HistoryMessage;
import com.example.server.domain.reward.repository.RewardHistoryRepository;
import com.example.server.domain.reward.service.command.CalculatePointAndExp;
import com.example.server.domain.reward.service.command.PointExperienceProvisionInformation;
import com.example.server.domain.user.entity.User;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.BadRequestException;
import com.example.server.global.exception.model.ConflictException;
import com.example.server.global.exception.model.NeurousException;
import com.example.server.global.exception.model.NotFoundException;
import com.example.server.global.redis.RedisUtil;
import com.example.server.global.storage.StorageConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

	private final QuizRepository quizRepository;
	private final QuizChoiceRepository quizChoiceRepository;
	private final UserRepository userRepository;
	private final QuizSolveRepository quizSolveRepository;
	private final ReadContentRepository readContentRepository;
	private final RewardHistoryRepository rewardHistoryRepository;
	private final StorageConfig storageConfig;
	private final RedisUtil redisUtil;

	/**
	 * 퀴즈 문제지 출제
	 */
	public QuizQuestionResponse getQuiz(Long userId, Long contentId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND));

		ContentLevel quizDiff = user.getContentLevel();

		Quiz quiz = quizRepository.findQuiz(contentId, quizDiff)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.QUIZ_NOT_FOUND_FOR_CONTENT_LEVEL));

		List<QuizChoiceResponse> choices = quizChoiceRepository.findByQuiz_QuizIdOrderByChoiceNoAsc(quiz.getQuizId())
			.stream()
			.map(QuizChoiceResponse::from)
			.toList();

		return QuizQuestionResponse.of(quiz, choices);
	}

	/**
	 * 퀴즈 정답 검증 및 보상 지급
	 */
	@Transactional
	public QuizSubmitResponse submit(Long userId, QuizSubmitRequest request) {
		// 읽기 기록 확인
		ReadContent readContent = checkReadContentAndFindReadContentById(userId, request.getReadContentId());

		// 퀴즈 존재 확인
		Quiz quiz = quizRepository.findById(request.getQuizId())
			.orElseThrow(() -> new NotFoundException(ErrorMessage.QUIZ_NOT_FOUND));

		// 사용자가 선택한 보기가 유효한지 확인
		QuizChoice selected = quizChoiceRepository.findByQuiz_QuizIdAndChoiceNo(request.getQuizId(),
				request.getSelectedNo())
			.orElseThrow(() -> new BadRequestException(ErrorMessage.QUIZ_INVALID_CHOICE));

		boolean isAnswerCorrect = selected.isCorrect();
		User user = readContent.getUser();

		quizSolveRepository.save(QuizSolve.of(
			user, readContent, quiz.getQuizId(), request.getSelectedNo(), isAnswerCorrect, LocalDateTime.now()
		));

		redisUtil.incrementMissionCount(userId, MissionType.QUIZ_SOLVE);

		CalculatePointAndExp calculatePointAndExp = calculateEarnedPointAndExp(isAnswerCorrect, user);

		QuizChoice correct = quizChoiceRepository.findByQuiz_QuizIdAndIsCorrectTrue(request.getQuizId())
			.orElseThrow(() -> new NeurousException(ErrorMessage.QUIZ_CORRECT_ANSWER_NOT_CONFIGURED));

		QuizResultResponse quizResultResponse = QuizResultResponse.builder()
			.quizId(request.getQuizId())
			.selectedNo(request.getSelectedNo())
			.isAnswerCorrect(isAnswerCorrect)
			.correctChoiceNo(correct.getChoiceNo())
			.correctChoiceText(correct.getChoiceText())
			.build();

		LevelUpInfo levelUpInfo = calculatePointAndExp.isLevelUp() ?
			LevelUpInfo.of(
				storageConfig.getProfileUrl(user.getProfileImgFileName()),
				user.getCharacterLevel().toString(),
				user.getCharacterLevel().getCharacterName()
			) : null;

		return QuizSubmitResponse.builder()
			.quizResultResponse(quizResultResponse)
			.rewardResponse(calculatePointAndExp.rewardResponse())
			.userLevelInformation(levelUpInfo)
			.build();
	}

	private ReadContent checkReadContentAndFindReadContentById(Long userId, Long readContentId) {
		ReadContent readContent = readContentRepository.findByIdWithUser(readContentId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.READ_RECORD_NOT_FOUND));

		if (!readContent.getUser().getId().equals(userId)) {
			throw new BadRequestException(ErrorMessage.INVALID_USER_READ_RECORD);
		}

		if (quizSolveRepository.existsByReadContent_ReadContentId(readContentId)) {
			throw new ConflictException(ErrorMessage.QUIZ_ALREADY_SOLVED);
		}
		return readContent;
	}

	private CalculatePointAndExp calculateEarnedPointAndExp(boolean isAnswerCorrect, User user) {
		int point = isAnswerCorrect ? PointExperienceProvisionInformation.CORRECT_ANSWER_POINT
			: PointExperienceProvisionInformation.WRONG_ANSWER_POINT;
		int exp = isAnswerCorrect ? PointExperienceProvisionInformation.CORRECT_ANSWER_EXPERIENCE
			: PointExperienceProvisionInformation.WRONG_ANSWER_EXPERIENCE;

		HistoryMessage message = isAnswerCorrect ? HistoryMessage.QUIZ_ANSWERS : HistoryMessage.QUIZ_CHALLENGE;

		rewardHistoryRepository.save(RewardHistory.create(user, point, exp, message));

		boolean isLevelUp = user.addPointAndExp(point, exp);

		return CalculatePointAndExp.of(new RewardResponse(point, exp), isLevelUp);
	}
}
