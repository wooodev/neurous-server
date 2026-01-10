package com.example.server.domain.mypage.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.server.domain.content.entity.ReadContent;
import com.example.server.domain.content.repository.ReadContentRepository;
import com.example.server.domain.mypage.dto.response.MyPageResponse;
import com.example.server.domain.mypage.dto.response.WeeklyReadCardResponse;
import com.example.server.domain.user.entity.User;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NotFoundException;
import com.example.server.global.storage.StorageConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {

	private final UserRepository userRepository;
	private final ReadContentRepository readContentRepository;
	private final StorageConfig storageConfig;

	public MyPageResponse getMyPageInfo(Long userId, LocalDateTime startOfWeek, LocalDateTime endOfWeek) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND));

		//주간 히스토리 * 읽은 콘텐츠 조회
		List<ReadContent> weeklyHistoryReadContent = readContentRepository.findWeeklyHistory(userId, startOfWeek,
			endOfWeek);

		//isCompleted=true * 다 읽은 콘텐츠만 노출
		List<ReadContent> completedContents = weeklyHistoryReadContent.stream()
			.filter(ReadContent::isCompleted)
			.toList();

		List<WeeklyReadCardResponse> cardResponses = completedContents.stream()
			.map(rc -> WeeklyReadCardResponse.builder()
				.contentId(rc.getContent().getContentId())
				.title(rc.getContent().getTitle())
				.category(rc.getContent().getContentCategory().getDescription())
				.readAt(rc.getReadAt())
				.isQuizCorrect(rc.getQuizSolve() != null ? rc.getQuizSolve().isAnswerCorrect() : null)
				.build())
			.toList();

		return MyPageResponse.builder()
			.profileImgUrl(storageConfig.getProfileUrl(user.getProfileImgFileName()))
			.name(user.getName())
			.email(user.getEmail())
			.interests(user.getInterests().stream()
				.map(ui -> ui.getInterest().getDescription())
				.toList())
			.level(user.getLevel())
			.weeklyCount(completedContents.size())
			.contents(cardResponses)
			.build();
	}
}


