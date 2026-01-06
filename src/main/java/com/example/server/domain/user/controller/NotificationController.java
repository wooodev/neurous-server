package com.example.server.domain.user.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.server.domain.user.controller.docs.NotificationControllerDocs;
import com.example.server.domain.user.controller.dto.response.NotificationResponse;
import com.example.server.domain.user.service.NotificationService;
import com.example.server.global.annotation.CurrentUserId;
import com.example.server.global.exception.dto.SuccessResponse;
import com.example.server.global.exception.message.SuccessMessage;
import com.example.server.global.security.annotation.AuthenticatedApi;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationControllerDocs {

	private final NotificationService notificationService;

	@AuthenticatedApi(reason = "알림 설정을 위해 로그인 필요")
	@PatchMapping("/toggle")
	public SuccessResponse<Boolean> toggleNotification(@CurrentUserId Long userId) {
		boolean currentStatus = notificationService.setNotification(userId);
		return SuccessResponse.of(
			SuccessMessage.UPDATE_SUCCESS_NOTIFICATION_SETTING,
			currentStatus
		);
	}

	@AuthenticatedApi(reason = "실시간 알림 구독을 위해 로그인 필요")
	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@CurrentUserId Long userId) {
		return notificationService.subscribe(userId);
	}

	@AuthenticatedApi(reason = "알림 내역 조회를 위해 로그인 필요")
	@GetMapping
	public SuccessResponse<List<NotificationResponse>> getNotifications(@CurrentUserId Long userId) {
		List<NotificationResponse> notifications = notificationService.getMyNotifications(userId);
		return SuccessResponse.of(
			SuccessMessage.GET_SUCCESS_NOTIFICATION_LIST,
			notifications
		);
	}

	@AuthenticatedApi(reason = "알림 읽음 처리를 위해 로그인 필요")
	@PatchMapping("/{notificationId}/read")
	public SuccessResponse<Void> readNotification(
		@CurrentUserId Long userId,
		@PathVariable Long notificationId
	) {
		notificationService.readNotification(userId, notificationId);
		return SuccessResponse.of(
			SuccessMessage.UPDATE_SUCCESS_NOTIFICATION_READ,
			null
		);
	}
}
