package com.example.server.domain.mission.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.example.server.domain.mission.entity.Mission;
import com.example.server.domain.mission.entity.vo.MissionType;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.server.domain.content.entity.Content;
import com.example.server.domain.content.entity.vo.ContentCategory;
import com.example.server.domain.content.entity.vo.ContentLevel;
import com.example.server.domain.content.repository.ContentRepository;
import com.example.server.domain.mission.dto.response.MissionContentResponse;
import com.example.server.domain.mission.dto.response.MissionProgressResponse;
import com.example.server.domain.mission.dto.response.MissionResponse;
import com.example.server.domain.mission.repository.MissionRepository;
import com.example.server.domain.user.entity.User;
import com.example.server.domain.user.entity.vo.UserInterest;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NotFoundException;
import com.example.server.global.redis.RedisUtil;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toSet;

@Service
@Transactional
@RequiredArgsConstructor
public class MissionService {

	private final UserRepository userRepository;
	private final ContentRepository contentRepository;
	private final MissionRepository missionRepository;

	private final RedisUtil redisUtil;

	public MissionResponse loadMissionPage(Long userId) {
		User user = findByUserId(userId);

		ensureMissionsExist(user);

		// 유저 맞춤 콘텐츠 3개 추출
		List<MissionContentResponse> contents = findMissionContent(userId);

		// 미션 진행 상태 조회 (기존에 작성한 MissionProgressResponse 활용)
		List<MissionProgressResponse> progresses = missionRepository.findAllByUser(user)
			.stream()
			.map(mission -> {
				int currentCount = redisUtil.getMissionCount(userId, mission.getMissionType());
				// Redis 데이터를 기반으로 응답 객체 생성
				return MissionProgressResponse.from(mission, currentCount);
			})
			.toList();

		return MissionResponse.of(contents, progresses);
	}

	public List<MissionContentResponse> findMissionContent(Long userId) {
		User user = findByUserId(userId);
		List<UserInterest> interests = findUserInterestById(userId);
		ContentLevel userLevel = ContentLevel.from(user.getLevel().name()); // 유저 레벨 변환

		List<Content> resultContents = new ArrayList<>();
		int totalCount = interests.size();

		if (totalCount == 1) {
			// 1순위 키워드에서 3개
			resultContents.addAll(fetchContents(userId,userLevel, interests.get(0), 3));
		} else if (totalCount == 2) {
			// 1순위 2개 / 2순위 1개
			resultContents.addAll(fetchContents(userId, userLevel, interests.get(0), 2));
			resultContents.addAll(fetchContents(userId, userLevel, interests.get(1), 1));
		} else if (totalCount >= 3) {
			resultContents.addAll(fetchContents(userId, userLevel, interests.get(0), 1));
			resultContents.addAll(fetchContents(userId, userLevel, interests.get(1), 1));
			resultContents.addAll(fetchContents(userId, userLevel, interests.get(2), 1));
		}

		return resultContents.stream()
				.map(c -> new MissionContentResponse(
					c.getTitle(),
					c.getImageUrl(),
					c.getContentCategory().name(),
					c.getCreatedAt().toLocalDate(),
					c.getContentId()
				)).toList();
	}

	//콘텐츠 추출 로직
	private List<Content> fetchContents(Long userId, ContentLevel level, UserInterest interest, int size) {

		ContentCategory category = ContentCategory.valueOf(interest.getInterest().name());

		return contentRepository.findByCategoryAndLevelExcludeReadByUser(
				userId,
				level,
				category,
				PageRequest.of(0, size)
		);
	}

	public User findByUserId(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND));
	}

	public List<UserInterest> findUserInterestById(Long userId) {
		return userRepository.findAllInterestsByUserId(userId);
	}

	private void ensureMissionsExist(User user) {
		List<Mission> existing = missionRepository.findAllByUser(user);
		Set<MissionType> existingTypes = existing.stream()
				.map(Mission::getMissionType)
				.collect(toSet());

		List<Mission> toCreate = new ArrayList<>();
		for (MissionType type : MissionType.values()) {
			if (!existingTypes.contains(type)) {
				toCreate.add(Mission.create(user, type, type.getDefaultGoalCount()));
			}
		}

		if (!toCreate.isEmpty()) {
			missionRepository.saveAll(toCreate);
		}
	}
}
