package com.example.server.domain.quiz.controller;

import com.example.server.domain.quiz.dto.QuizQuestionResponse;
import com.example.server.domain.quiz.dto.QuizSubmitRequest;
import com.example.server.domain.quiz.dto.QuizSubmitResponse;
import com.example.server.domain.quiz.service.QuizService;
import com.example.server.global.annotation.CurrentUserId;
import com.example.server.global.exception.dto.SuccessResponse;
import com.example.server.global.exception.message.SuccessMessage;
import com.example.server.global.security.annotation.AuthenticatedApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    @AuthenticatedApi
    @GetMapping("/set")
    public SuccessResponse<QuizQuestionResponse> getQuiz(
            @CurrentUserId Long userId,
            @PathVariable int contentId
    ) {
        QuizQuestionResponse result = quizService.getQuiz(userId, contentId);
        return SuccessResponse.of(SuccessMessage.LOAD_SUCCESS, result);
    }

    @AuthenticatedApi
    @PostMapping("/quiz/{quizId}/submit")
    public SuccessResponse<QuizSubmitResponse> submit(
            @CurrentUserId Long userId,
            @PathVariable int quizId,
            @RequestBody QuizSubmitRequest request
    ) {
        QuizSubmitResponse result = quizService.submit(userId, quizId, request.selectedNo());
        return SuccessResponse.of(SuccessMessage.LOAD_SUCCESS, result);
    }
}
