package com.example.server.domain.auth.controller.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.server.domain.auth.controller.dto.response.RefreshResponse;
import com.example.server.global.docs.ApiErrorStandard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
	summary = "Access Token 재발급",
	description = "Refresh Token을 매개변수에 전달하여 Access Token 재발급"
)
@ApiErrorStandard
@ApiResponse(
	responseCode = "200",
	description = "토큰 재발급 성공",
	content = @Content(schema = @Schema(implementation = RefreshResponse.class))
)
public @interface RefreshTokenDocs {
}
