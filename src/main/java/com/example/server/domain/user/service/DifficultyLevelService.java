package com.example.server.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.server.domain.user.dto.response.LoadDifficultyLevel;
import com.example.server.domain.user.entity.DifficultyLevel;
import com.example.server.domain.user.entity.vo.Level;
import com.example.server.domain.user.repository.DifficultyLevelRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DifficultyLevelService {

	private final DifficultyLevelRepository difficultyLevelRepository;

	public LoadDifficultyLevel loadDifficultyLevel(Level level) {
		DifficultyLevel difficultyLevel = difficultyLevelRepository.findByLevel(level)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_LEVEL_DESCRIPTION));
		return LoadDifficultyLevel.from(difficultyLevel);
	}
}
