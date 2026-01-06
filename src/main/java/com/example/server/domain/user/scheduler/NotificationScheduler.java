package com.example.server.domain.user.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.server.domain.user.entity.User;
import com.example.server.domain.user.entity.vo.NotificationMessage;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.domain.user.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

	private final UserRepository userRepository;
	private final NotificationService notificationService;

	// 매일 오전 8시 (Cron: 초 분 시 일 월 요일)
	@Scheduled(cron = "0 0 8 * * *")
	public void sendMorningNotification() {
		log.info("오전 8시 정기 알림 발송 시작");
		// 1안과 2안 중 랜덤 선택 (또는 로직에 따라 선택)
		NotificationMessage message = Math.random() < 0.5 ?
			NotificationMessage.MORNING_1 : NotificationMessage.MORNING_2;

		sendBatchNotifications(message);
	}

	// 매일 오후 6시 (18시)
	@Scheduled(cron = "0 0 18 * * *")
	public void sendEveningNotification() {
		log.info("오후 6시 정기 알림 발송 시작");
		NotificationMessage message = Math.random() < 0.5 ?
			NotificationMessage.EVENING_1 : NotificationMessage.EVENING_2;

		sendBatchNotifications(message);
	}

	private void sendBatchNotifications(NotificationMessage message) {
		List<User> targetUsers = userRepository.findByNotificationStatusTrue();

		for (User user : targetUsers) {
			try {
				// 기존에 만든 서비스 메서드 호출 (DB 저장 + SSE 전송)
				notificationService.createAndSendNotification(
					user.getId(),
					message.getTitle(),
					message.getBody()
				);
			} catch (Exception e) {
				log.error("사용자 {}에게 알림 전송 실패: {}", user.getId(), e.getMessage());
			}
		}
		log.info("총 {}명의 사용자에게 알림 발송 완료", targetUsers.size());
	}
}
