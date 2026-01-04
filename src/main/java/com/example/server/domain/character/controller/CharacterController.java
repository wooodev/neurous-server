package com.example.server.domain.character.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.server.domain.character.controller.docs.CharacterControllerDocs;
import com.example.server.domain.character.dto.CharacterPageResponse;
import com.example.server.domain.character.dto.CheckLevelStandardResponse;
import com.example.server.domain.character.dto.RewardHistoryResponse;
import com.example.server.domain.character.dto.RewardInformationResponse;
import com.example.server.domain.character.service.CharacterService;
import com.example.server.global.annotation.CurrentUserId;
import com.example.server.global.exception.dto.SuccessResponse;
import com.example.server.global.exception.message.SuccessMessage;
import com.example.server.global.security.annotation.AuthenticatedApi;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController implements CharacterControllerDocs {

	private final CharacterService characterService;

	@AuthenticatedApi(reason = "캐릭터 페이지 데이터 조회를 위해 로그인 필요")
	@GetMapping("/me")
	public SuccessResponse<CharacterPageResponse> getCharacterPage(
		@CurrentUserId Long userId
	) {
		return SuccessResponse.of(
			SuccessMessage.LOAD_SUCCESS_CHARACTER_PAGE, // SuccessMessage에 해당 상수 추가 필요
			characterService.getCharacterPageData(userId)
		);
	}

	@AuthenticatedApi(reason = "레벨 기준 데이터 조회를 위해 로그인 필요")
	@GetMapping("/standards/level")
	public SuccessResponse<CheckLevelStandardResponse> getLevelStandards(
		@CurrentUserId Long userId
	) {
		return SuccessResponse.of(
			SuccessMessage.LOAD_SUCCESS_LEVEL_STANDARD,
			characterService.checkLevelStandard(userId)
		);
	}

	@AuthenticatedApi(reason = "보상 내역 조회를 위해 로그인 필요")
	@GetMapping("/history")
	public SuccessResponse<List<RewardHistoryResponse>> getRewardHistories(
		@CurrentUserId Long userId
	) {
		return SuccessResponse.of(
			SuccessMessage.LOAD_SUCCESS_REWARD_HISTORY,
			characterService.getRewardHistories(userId)
		);
	}

	@AuthenticatedApi(reason = "보상 기준 데이터 조회를 위해 로그인 필요")
	@GetMapping("/standards/reward")
	public SuccessResponse<List<RewardInformationResponse>> getRewardStandards() {
		return SuccessResponse.of(
			SuccessMessage.LOAD_SUCCESS_REWARD_STANDARD,
			characterService.checkPointExpStandard()
		);
	}

}
