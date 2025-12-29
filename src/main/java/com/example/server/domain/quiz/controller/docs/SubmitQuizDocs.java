package com.example.server.domain.quiz.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "퀴즈 정답 제출",
        description = "사용자가 선택한 객관식 번호(selectedNo)를 제출하고 정답 여부를 반환합니다."
)
@ApiResponse(
        responseCode = "200",
        description = "제출 성공"
)
@ApiResponse(
        responseCode = "400",
        description = "존재하지 않는 선택지(selectedNo가 유효하지 않음)"
)
@ApiResponse(
        responseCode = "401",
        description = "인증 실패/세션 만료"
)
@ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 퀴즈"
)
@ApiResponse(
        responseCode = "409",
        description = "이미 퀴즈를 제출했습니다."
)
@ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류"
)
public @interface SubmitQuizDocs {
}
