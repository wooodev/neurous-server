package com.example.server.domain.user.controller.docs;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.server.domain.user.controller.dto.response.NotificationResponse;
import com.example.server.global.exception.dto.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notification", description = "실시간 알림 및 설정 API")
public interface NotificationControllerDocs {

	@ToggleNotificationDocs
	SuccessResponse<Boolean> toggleNotification(
		@Parameter(hidden = true) Long userId
	);

	@SubscribeNotificationDocs
	SseEmitter subscribe(
		@Parameter(hidden = true) Long userId
	);

	@Operation(summary = "알림 목록 조회", description = "최근 7일간의 알림 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "알림 목록 조회 성공")
	SuccessResponse<List<NotificationResponse>> getNotifications(
		@Parameter(hidden = true) Long userId
	);

	@Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
	@ApiResponse(responseCode = "200", description = "읽음 처리 성공")
	SuccessResponse<Void> readNotification(
		@Parameter(hidden = true) Long userId,
		@PathVariable Long notificationId
	);
}
