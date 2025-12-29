package com.example.server.domain.quiz.service;

import com.example.server.domain.quiz.dto.QuizChoiceResponse;
import com.example.server.domain.quiz.dto.QuizQuestionResponse;
import com.example.server.domain.quiz.dto.QuizSubmitResponse;
import com.example.server.domain.quiz.entity.Quiz;
import com.example.server.domain.quiz.entity.QuizChoice;
import com.example.server.domain.quiz.entity.QuizSolve;
import com.example.server.domain.quiz.repository.QuizChoiceRepository;
import com.example.server.domain.quiz.repository.QuizRepository;
import com.example.server.domain.quiz.repository.QuizSolveRepository;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.BadRequestException;
import com.example.server.global.exception.model.ConflictException;
import com.example.server.global.exception.model.NeurousException;
import com.example.server.global.exception.model.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizChoiceRepository quizChoiceRepository;
    private final UserRepository userRepository;
    private final QuizSolveRepository quizSolveRepository;

    /**
    * 퀴즈 문제지 출제
    */
    public QuizQuestionResponse getQuiz(Long userId, int contentId) {

        String quizDiff = userRepository.findLevelByUserId(userId).orElse("초급");

        Quiz quiz = quizRepository.findByContentIdAndQuizDiff(contentId, quizDiff)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.QUIZ_NOT_FOUND_FOR_CONTENT_LEVEL));

        List<QuizChoiceResponse> choices = quizChoiceRepository.findByQuizIdOrderByChoiceNoAsc(quiz.getQuizId())
                .stream()
                .map(QuizChoiceResponse::from)
                .toList();

        QuizQuestionResponse response = QuizQuestionResponse.of(quiz, choices);

        return response;
    }

    /**
     * 퀴즈 정답 검증
     */
    public QuizSubmitResponse submit(Long userId, int quizId, int selectedNo) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.QUIZ_NOT_FOUND));

        int contentId = quiz.getContentId();
        if (quizSolveRepository.findByUserIdAndContentId(userId, contentId).isPresent()) {
            throw new ConflictException(ErrorMessage.QUIZ_ALREADY_SOLVED);
        }

        QuizChoice selected = quizChoiceRepository.findByQuizIdAndChoiceNo(quizId, selectedNo)
                .orElseThrow(() -> new BadRequestException(ErrorMessage.QUIZ_INVALID_CHOICE));

        QuizChoice correct = quizChoiceRepository.findByQuizIdAndIsCorrectTrue(quizId)
                .orElseThrow(() -> new NeurousException(ErrorMessage.QUIZ_CORRECT_ANSWER_NOT_CONFIGURED));

        boolean isAnswerCorrect = Boolean.TRUE.equals(selected.getIsCorrect());

        QuizSolve solve = QuizSolve.of(userId, contentId, quizId, selectedNo, isAnswerCorrect, LocalDateTime.now());

        quizSolveRepository.save(solve);

        QuizSubmitResponse response = QuizSubmitResponse.of(quizId, selectedNo, isAnswerCorrect, correct);

        return response;
    }
}
