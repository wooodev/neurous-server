package com.example.server.domain.user.controller;

import com.example.server.domain.user.dto.request.WithdrawRequest;
import org.springframework.web.bind.annotation.*;

import com.example.server.domain.user.controller.docs.UserControllerDocs;
import com.example.server.domain.user.dto.request.UpdateInterestsRequest;
import com.example.server.domain.user.dto.request.UpdateLevelRequest;
import com.example.server.domain.user.dto.response.UserInterestsResponse;
import com.example.server.domain.user.service.UserService;
import com.example.server.global.annotation.CurrentUserId;
import com.example.server.global.exception.dto.SuccessResponse;
import com.example.server.global.exception.message.SuccessMessage;
import com.example.server.global.security.annotation.AuthenticatedApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Slf4j
public class UserController implements UserControllerDocs {

	private final UserService userService;

	@AuthenticatedApi
	@PatchMapping("/update/interest")
	public SuccessResponse<UserInterestsResponse> updateInterest(
			@RequestBody @Valid UpdateInterestsRequest request,
			@CurrentUserId Long userId) {
		UserInterestsResponse response = userService.updateInterest(userId, request);
		return SuccessResponse.of(SuccessMessage.UPDATE_SUCCESS, response);
	}

	@AuthenticatedApi
	@PatchMapping("/update/level")
	public SuccessResponse<Void> updateLevel(
			@RequestBody @Valid UpdateLevelRequest request,
			@CurrentUserId Long userId) {
		userService.updateLevel(userId, request.level());
		return SuccessResponse.of(SuccessMessage.UPDATE_SUCCESS);
	}

	@AuthenticatedApi
	@DeleteMapping("/withdraw")
	public SuccessResponse<Void> withdraw(
			@RequestBody @Valid WithdrawRequest request,
			@CurrentUserId Long userId
	) {
		userService.withdraw(userId, request);
		return SuccessResponse.of(SuccessMessage.WITHDRAW_SUCCESS);
	}

}