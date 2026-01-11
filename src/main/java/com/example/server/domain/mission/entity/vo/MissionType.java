package com.example.server.domain.mission.entity.vo;

import lombok.Getter;

@Getter
public enum MissionType {
	// 탐색 화면에서 글 1개 읽기
	EXPLORE_READ(MissionCategory.READ, 1, "탐색에서 글 읽기"),

	// 퀴즈 3개 풀기
	QUIZ_SOLVE(MissionCategory.QUIZ, 3, "퀴즈 풀기"),

	// 홈 화면에서 글 3개 읽기
	HOME_READ(MissionCategory.READ, 3, "홈에서 글 읽기"),

	EARN_POINT(MissionCategory.POINT, 60, "60P 포인트 획득하기"),

	READ_CONTENT_CHECK(MissionCategory.READ, 1, "읽은 글 확인하기");

	private final MissionCategory category; // READ, QUIZ 등
	private final int defaultGoalCount;     // 기본 목표 개수 (1개, 3개 등)
	private final String description;

	MissionType(MissionCategory category, int defaultGoalCount, String description) {
		this.category = category;
		this.defaultGoalCount = defaultGoalCount;
		this.description = description;
	}
}
