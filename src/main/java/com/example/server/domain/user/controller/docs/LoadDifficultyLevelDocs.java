package com.example.server.domain.user.controller.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.server.domain.user.dto.response.LoadDifficultyLevel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
	summary = "난이도 설명 조회",
	description = "선택한 난이도(Level)에 대한 설명과 시간 가이드를 조회합니다."
)
@ApiResponse(
	responseCode = "200",
	description = "조회 성공",
	content = @Content(schema = @Schema(implementation = LoadDifficultyLevel.class))
)
@ApiResponse(
	responseCode = "404",
	description = "선택한 레벨에 대한 정보가 없습니다"
)
public @interface LoadDifficultyLevelDocs {
}
