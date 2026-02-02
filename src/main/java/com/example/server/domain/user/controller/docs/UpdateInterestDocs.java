package com.example.server.domain.user.controller.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.server.domain.user.dto.response.UserInterestsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
	summary = "관심분야 설정",
	description = "사용자의 관심분야를 3개 선택하여 우선순위와 함께 저장합니다."
)
@ApiResponse(
	responseCode = "200",
	description = "관심분야 설정 성공",
	content = @Content(schema = @Schema(implementation = UserInterestsResponse.class))
)
@ApiResponse(
	responseCode = "400",
	description = "관심분야는 3가지 모두 선택해야합니다"
)
@ApiResponse(
	responseCode = "400",
	description = "중복된 관심분야 항목이 있습니다"
)
@ApiResponse(responseCode = "401",
	description = "세션이 만료되었습니다. 다시 로그인해주세요"
)
public @interface UpdateInterestDocs {
}
