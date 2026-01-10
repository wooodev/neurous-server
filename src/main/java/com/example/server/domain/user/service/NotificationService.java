package com.example.server.domain.user.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.server.domain.user.controller.dto.response.NotificationResponse;
import com.example.server.domain.user.entity.Notification;
import com.example.server.domain.user.entity.User;
import com.example.server.domain.user.repository.NotificationRepository;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;
	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	@Transactional
	public boolean setNotification(Long userId) {
		User user = findByUserId(userId);
		user.toggleNotification();
		return user.isNotificationStatus();
	}

	// 1. 알림 리스트 조회 (최대 7일전까지)
	@Transactional
	public List<NotificationResponse> getMyNotifications(Long userId) {
		LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
		return notificationRepository.findRecentNotifications(userId, sevenDaysAgo)
			.stream()
			.map(NotificationResponse::from)
			.toList();
	}

	@Transactional
	public void createAndSendNotification(Long userId, String title, String content) {
		User user = findByUserId(userId);

		if (user.isNotificationStatus()) {
			// DB 저장
			Notification notification = Notification.builder()
				.userId(userId)
				.title(title)
				.content(content)
				.build();
			notificationRepository.save(notification);

			// 실시간 SSE 전송
			sendToClient(userId, "NOTIFICATION", NotificationResponse.from(notification));
		}
	}

	@Transactional
	public void readNotification(Long userId, Long notificationId) {
		Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_NOT_FOUND));

		notification.markAsRead(); // Notification 엔티티에 정의한 isRead = true 메서드
	}

	// 알림 구독 (SSE)
	public SseEmitter subscribe(Long userId) {
		SseEmitter emitter = new SseEmitter(60 * 1000L * 60);
		emitters.put(userId, emitter);

		emitter.onCompletion(() -> emitters.remove(userId));
		emitter.onTimeout(() -> emitters.remove(userId));

		sendToClient(userId, "connect", "connected");
		return emitter;
	}

	private void sendToClient(Long userId, String eventName, Object data) {
		SseEmitter emitter = emitters.get(userId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event().name(eventName).data(data));
			} catch (IOException e) {
				emitters.remove(userId);
				log.error("SSE 전송 실패로 연결을 제거합니다. userId: {}", userId);
			}
		}
	}

	private User findByUserId(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND));
	}
}
