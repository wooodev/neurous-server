package com.example.server.domain.user.controller.docs;

import com.example.server.domain.user.controller.dto.request.WithdrawRequest;
import com.example.server.global.exception.message.SuccessMessage;
import com.example.server.global.security.annotation.AuthenticatedApi;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.server.domain.user.controller.dto.request.UpdateInterestsRequest;
import com.example.server.domain.user.controller.dto.request.UpdateLevelRequest;
import com.example.server.domain.user.controller.dto.response.UserInterestsResponse;
import com.example.server.global.annotation.CurrentUserId;
import com.example.server.global.exception.dto.SuccessResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[마이페이지 / 회원가입] 난이도 / 관심분야 관련 API ", description = "난이도 / 관심분야 설정 및 수정 API ")
public interface UserControllerDocs {

	@UpdateInterestDocs
	SuccessResponse<UserInterestsResponse> updateInterest(
			@RequestBody @Valid UpdateInterestsRequest request,
			@CurrentUserId Long userId);

	@UpdateLevelDocs
	SuccessResponse<Void> updateLevel(
			@RequestBody @Valid UpdateLevelRequest request,
			@CurrentUserId Long userId);

	@WithdrawDocs
	public SuccessResponse<Void> withdraw(
			@RequestBody @Valid WithdrawRequest request,
			@CurrentUserId Long userId
	) ;
}