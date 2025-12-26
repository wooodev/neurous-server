package com.example.server.domain.content.controller.docs;

import com.example.server.domain.content.entity.ReadContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "콘텐츠 읽음 처리",
        description = """
                사용자가 특정 콘텐츠를 읽었음을 기록합니다.
                - 인증이 필요한 API 입니다.
                - contentId가 존재하지 않으면 404를 반환합니다.
                - (주의) 현재 구현은 중복 호출 시 중복 저장/유니크 제약에 따라 409가 발생할 수 있습니다.
                """
)
@ApiResponse(
        responseCode = "200",
        description = "읽음 처리 성공",
        content = @Content(schema = @Schema(implementation = ReadContent.class))
)
@ApiResponse(
        responseCode = "401",
        description = "인증 실패 (토큰 누락/만료)"
)
@ApiResponse(
        responseCode = "404",
        description = "콘텐츠를 찾을 수 없습니다."
)
@ApiResponse(
        responseCode = "409",
        description = "이미 읽음 처리된 콘텐츠입니다."
)
public @interface SetContentReadDocs {
}