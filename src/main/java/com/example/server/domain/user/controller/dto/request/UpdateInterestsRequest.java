package com.example.server.domain.user.controller.dto.request;

import java.util.List;

import com.example.server.domain.user.entity.vo.UserField;

import jakarta.validation.constraints.Size;

public record UpdateInterestsRequest(
	@Size(min = 1, max = 3, message = "관심분야는 1~3개를 선택합니다.")
	List<UserField> interests
) {
}
