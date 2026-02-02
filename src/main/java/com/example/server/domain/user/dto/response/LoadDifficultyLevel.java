package com.example.server.domain.user.dto.response;

import com.example.server.domain.user.entity.DifficultyLevel;
import com.example.server.domain.user.entity.vo.Level;

public record LoadDifficultyLevel(
	String key,
	String level,
	String description,
	String timeGuide
) {
	public static LoadDifficultyLevel from(DifficultyLevel level) {
		Level levelEnum = level.getLevel();

		return new LoadDifficultyLevel(
			levelEnum.name(),               // key
			levelEnum.getDescription(),     // level(초급/중급/고급)
			level.getDescription(),
			level.getTimeGuide()
		);
	}
}
