package com.example.server.domain.user.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.example.server.domain.user.entity.Notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
	private Long id;
	private String title;
	private String content;
	private String displayDate; // n일 전 혹은 날짜 표시
	private boolean isRead;

	public static NotificationResponse from(Notification notification) {
		return NotificationResponse.builder()
			.id(notification.getId())
			.title(notification.getTitle())
			.content(notification.getContent())
			.displayDate(formatDisplayDate(notification.getCreatedAt()))
			.isRead(notification.isRead())
			.build();
	}

	private static String formatDisplayDate(LocalDateTime createdAt) {
		long daysBetween = ChronoUnit.DAYS.between(createdAt.toLocalDate(), LocalDate.now());

		if (daysBetween == 0)
			return "오늘";
		if (daysBetween <= 3)
			return daysBetween + "일 전";
		return createdAt.toLocalDate().toString(); // 4일 전부터는 날짜(2026-01-04) 표시
	}
}
