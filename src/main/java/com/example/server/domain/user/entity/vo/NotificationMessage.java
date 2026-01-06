package com.example.server.domain.user.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationMessage {
	MORNING_1("가벼운 글 읽기로 하루를 시작해요", "관심사에 딱 맞는 오늘의 글이 도착했어요"),
	MORNING_2("오늘의 첫 글, 3분이면 충분해요", "오늘의 글이 새로 도착했어요"),
	EVENING_1("가볍게 하루를 마무리해요", "지금 읽을 수 있는 글들이 있어요"),
	EVENING_2("지금도 읽을 수 있어요", "지금 읽기 좋은 글들이 준비되어 있어요");

	private final String title;
	private final String body;
}
