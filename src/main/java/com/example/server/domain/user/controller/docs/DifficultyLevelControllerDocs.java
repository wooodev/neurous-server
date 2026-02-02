package com.example.server.domain.user.controller.docs;

import org.springframework.web.bind.annotation.PathVariable;

import com.example.server.domain.user.dto.response.LoadDifficultyLevel;
import com.example.server.domain.user.entity.vo.Level;
import com.example.server.global.exception.dto.SuccessResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[난이도] ", description = "난이도 설명 조회 API")
public interface DifficultyLevelControllerDocs {

	@LoadDifficultyLevelDocs
	SuccessResponse<LoadDifficultyLevel> loadDifficultyLevel(@PathVariable Level level);
}
