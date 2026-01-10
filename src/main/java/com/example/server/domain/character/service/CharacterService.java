package com.example.server.domain.character.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.server.domain.attendance.dto.WeeklyAttendanceResponse;
import com.example.server.domain.attendance.service.AttendanceService;
import com.example.server.domain.character.dto.CharacterPageResponse;
import com.example.server.domain.character.dto.CheckLevelStandardResponse;
import com.example.server.domain.character.dto.LevelStandardInformation;
import com.example.server.domain.character.dto.RewardHistoryResponse;
import com.example.server.domain.character.dto.RewardInformationResponse;
import com.example.server.domain.character.dto.UserGrowthInfo;
import com.example.server.domain.mission.dto.response.MissionProgressResponse;
import com.example.server.domain.mission.repository.MissionRepository;
import com.example.server.domain.reward.metadata.entity.RewardData;
import com.example.server.domain.reward.metadata.entity.RewardInfoData;
import com.example.server.domain.reward.metadata.repository.RewardDataRepository;
import com.example.server.domain.reward.metadata.repository.RewardInfoDataRepository;
import com.example.server.domain.reward.repository.RewardHistoryRepository;
import com.example.server.domain.user.entity.User;
import com.example.server.domain.user.entity.vo.CharacterLevel;
import com.example.server.domain.user.metadata.entity.CharacterData;
import com.example.server.domain.user.metadata.repository.CharacterDataRepository;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NotFoundException;
import com.example.server.global.redis.RedisUtil;
import com.example.server.global.storage.StorageConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CharacterService {

	private final RedisUtil redisUtil;
	private final UserRepository userRepository;
	private final RewardDataRepository rewardDataRepository;
	private final RewardInfoDataRepository rewardInfoDataRepository;
	private final RewardHistoryRepository rewardHistoryRepository;
	private final CharacterDataRepository characterdataRepository;

	private final MissionRepository missionRepository;
	private final StorageConfig storageConfig;
	private final AttendanceService attendanceService;

	//전체 응답 데이터 반환 (미션 진행 상태 조회 로직 포함)

	@Transactional
	public CharacterPageResponse getCharacterPageData(Long userId) {
		User user = findUserById(userId);

		// 레벨 기반 캐릭터 폴더 URL 생성 (예: baseUrl/character/LEVEL_1/)
		String characterVideoUrl = storageConfig.getCharacterUrl(user.getCharacterLevel().name());

		UserGrowthInfo growthInfo = UserGrowthInfo.of(user, characterVideoUrl);

		// 캐릭터 페이지 진입 시 레벨업 대기 상태 해제
		if (user.isPendingModuleLevelUp()) {
			user.completeLevelUpModuleDisplayInCharacterPage();
		}

		// 2. 미션 진행도 정보
		List<MissionProgressResponse> missions = missionRepository.findAllByUser(user)
			.stream()
			.map(mission -> {
				int currentCount = redisUtil.getMissionCount(userId, mission.getMissionType());
				return MissionProgressResponse.from(mission, currentCount);
			})
			.toList();

		// 3. 주간 출석 정보
		WeeklyAttendanceResponse attendance = attendanceService.getWeeklyAttendanceStatus(userId);

		return new CharacterPageResponse(growthInfo, attendance, missions);
	}

	@Transactional
	public int experienceBasedPercentageCalculate(Long userId) {
		User user = findUserById(userId);
		int nowProgress = user.calculateLevelProgress();

		if (nowProgress == 100) {
			// 현재 퍼센트가 아닌 '보유 경험치'로 다음 레벨을 판단해야 함
			CharacterLevel nextLevel = CharacterLevel.getLevelByExp(user.getExp());
			user.changeCharacterLevel(nextLevel);
		}

		return nowProgress;
	}

	//레벨 기준 확인
	public CheckLevelStandardResponse checkLevelStandard(Long userId) {

		User user = findUserById(userId);

		int currentUserExp = getCurrentUserExp(user); //현재 나의 경험치
		CharacterLevel currentUserCharacterLevel = getCurrentUserCharacterLevel(user); //현재 나의 레벨

		List<CharacterData> allLevels = characterdataRepository.findAll();

		List<LevelStandardInformation> levelInfo = new ArrayList<>();

		for (CharacterData characterData : allLevels) {
			LevelStandardInformation data = LevelStandardInformation.from(characterData);
			levelInfo.add(data);
		}

		return new CheckLevelStandardResponse(
			currentUserExp,
			currentUserCharacterLevel,
			levelInfo
		);
	}

	// 경험치 * 포인트 기준 확인
	public List<RewardInformationResponse> checkPointExpStandard() {

		List<RewardData> pointExpStandardData = rewardDataRepository.findAll();
		List<RewardInfoData> rewardInformationData = rewardInfoDataRepository.findAll();

		int size = Math.min(pointExpStandardData.size(), rewardInformationData.size());

		return IntStream.range(0, size)
			.mapToObj(i -> {
				RewardData data = pointExpStandardData.get(i);
				RewardInfoData info = rewardInformationData.get(i);

				return new RewardInformationResponse(
					new RewardInformationResponse.AboutPointExpInformation(
						info.getRewardType(),
						info.getRewardDescription()
					),
					new RewardInformationResponse.RewardDataResponse(
						data.getRewardItem(),
						data.getRewardExp(),
						data.getRewardPoint()
					)
				);
			})
			.toList();
	}

	public List<RewardHistoryResponse> getRewardHistories(Long userId) {
		// 최신순으로 내역 조회
		return rewardHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
			.stream()
			.map(RewardHistoryResponse::from)
			.collect(Collectors.toList());
	}

	public int getCurrentUserExp(User user) {
		return user.getExp();
	}

	public CharacterLevel getCurrentUserCharacterLevel(User user) {
		return user.getCharacterLevel();
	}

	public User findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND));
	}
}
