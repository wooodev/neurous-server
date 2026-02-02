package com.example.server.domain.user.dto.request;

import com.example.server.domain.user.entity.vo.Level;

import jakarta.validation.constraints.NotNull;

public record UpdateLevelRequest(
		@NotNull(message = "난이도는 선택은 필수입니다.")
		Level level
) {
}