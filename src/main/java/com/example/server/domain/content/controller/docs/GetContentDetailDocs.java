package com.example.server.domain.content.controller.docs;

import com.example.server.global.docs.ApiErrorStandard;
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
        summary = "콘텐츠 상세 조회",
        description = "콘텐츠 상세 정보를 조회합니다."
)
@ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = Content.class))
)
@ApiResponse(
        responseCode = "400",
        description = "요청 값이 유효하지 않음"
)
@ApiResponse(
        responseCode = "404",
        description = "리소스를 찾을 수 없음(컨텐츠 미존재)"
)
@ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류"
)
public @interface GetContentDetailDocs {
}