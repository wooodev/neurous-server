package com.example.server.domain.content.controller.docs;

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
        summary = "읽은 글 상세 조회",
        description = "읽은 글 상세 정보를 조회합니다."
)
@ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = Content.class))
)
@ApiResponse(
        responseCode = "404",
        description = "리소스를 찾을 수 없음(컨텐츠 미존재)"
)
@ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류"
)
public @interface GetReadDetailDocs {
}
