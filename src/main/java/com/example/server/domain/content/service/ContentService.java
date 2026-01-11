package com.example.server.domain.content.service;

import java.time.LocalDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.server.domain.attendance.service.AttendanceService;
import com.example.server.domain.content.dto.request.UpdateReadStatusRequest;
import com.example.server.domain.content.dto.response.ContentAccessResponse;
import com.example.server.domain.content.dto.response.ContentDetailResponse;
import com.example.server.domain.content.dto.response.ContentResponse;
import com.example.server.domain.content.dto.response.DifficultyRecommendResponse;
import com.example.server.domain.content.dto.response.ExploreResponse;
import com.example.server.domain.content.dto.response.ReadStatusResponse;
import com.example.server.domain.content.dto.response.RecentSearchResponse;
import com.example.server.domain.content.entity.Content;
import com.example.server.domain.content.entity.ContentDifficultyEvaluation;
import com.example.server.domain.content.entity.DifficultyBasetime;
import com.example.server.domain.content.entity.ReadContent;
import com.example.server.domain.content.entity.vo.ContentCategory;
import com.example.server.domain.content.entity.vo.ContentDifficulty;
import com.example.server.domain.content.entity.vo.ContentLevel;
import com.example.server.domain.content.entity.vo.DifficultyRecommend;
import com.example.server.domain.content.repository.ContentDifficultyEvaluationRepository;
import com.example.server.domain.content.repository.ContentRepository;
import com.example.server.domain.content.repository.DifficultyBasetimeRepository;
import com.example.server.domain.content.repository.ReadContentRepository;
import com.example.server.domain.content.service.command.DifficultyRecommendConfig;
import com.example.server.domain.content.service.command.ReadableContentLimitsInfo;
import com.example.server.domain.content.service.command.RequestRecommendContentMessage;
import com.example.server.domain.mission.entity.vo.MissionType;
import com.example.server.domain.quiz.dto.response.QuizChoiceResponse;
import com.example.server.domain.quiz.dto.response.ReadContentDetailResponse;
import com.example.server.domain.quiz.dto.response.SolvedQuizResponse;
import com.example.server.domain.quiz.entity.Quiz;
import com.example.server.domain.quiz.entity.QuizChoice;
import com.example.server.domain.quiz.repository.QuizChoiceRepository;
import com.example.server.domain.quiz.repository.QuizRepository;
import com.example.server.domain.quiz.repository.QuizSolveRepository;
import com.example.server.domain.reward.dto.response.LevelUpInfo;
import com.example.server.domain.reward.entity.RewardHistory;
import com.example.server.domain.reward.entity.vo.HistoryMessage;
import com.example.server.domain.reward.repository.RewardHistoryRepository;
import com.example.server.domain.reward.service.command.PointExperienceProvisionInformation;
import com.example.server.domain.user.entity.User;
import com.example.server.domain.user.entity.vo.Level;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.BadRequestException;
import com.example.server.global.exception.model.ConflictException;
import com.example.server.global.exception.model.NotFoundException;
import com.example.server.global.redis.RedisKey;
import com.example.server.global.redis.RedisUtil;
import com.example.server.global.storage.StorageConfig;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContentService {

	private final ContentRepository contentRepository;
	private final UserRepository userRepository;
	private final ReadContentRepository readContentRepository;
	private final ContentDifficultyEvaluationRepository contentDifficultyEvaluationRepository;
	private final DifficultyBasetimeRepository difficultyBasetimeRepository;
	private final QuizSolveRepository quizSolveRepository;
	private final QuizRepository quizRepository;
	private final QuizChoiceRepository quizChoiceRepository;
	private final RewardHistoryRepository rewardHistoryRepository;

	private final AttendanceService attendanceService;

	private final RedisUtil redisUtil;
	private final StorageConfig storageConfig;

	public ExploreResponse getExplore(Long userId, int page, int size){

		ContentLevel userLevel = getUserContentLevel(userId);
		LocalDateTime now = LocalDateTime.now();
		attendanceService.providedAttendanceRewardToday(now, userId);

		List<Content> all = new ArrayList<>();

		int fetchSizePerCategory = Math.max(10, (page + 1) * size);

		for (ContentCategory contentCategory : ContentCategory.values()) {
			List<Content> contents =
					contentRepository.findByCategoryAndLevel(userLevel, contentCategory, PageRequest.of(0, fetchSizePerCategory));
			all.addAll(contents);
		}

		List<ContentResponse> result = all.stream()
				.skip((long) page * size)
				.limit(size)
				.map(c -> ContentResponse.from(c, redisUtil.getHits(c.getContentId())))
				.toList();

		return ExploreResponse.builder()
				.contents(result)
				.build();
	}

	public ExploreResponse getExploreByCategory(Long userId, ContentCategory category, int page, int size) {
		ContentLevel userLevel = getUserContentLevel(userId);

		int fetchSize = Math.max(10, (page + 1) * size);

		List<Content> contents = contentRepository.findByCategoryAndLevel(
				userLevel,
				category,
				PageRequest.of(0, fetchSize));

		List<ContentResponse> result = contents.stream()
				.skip((long) page * size)
				.limit(size)
				.map(c -> ContentResponse.from(c, redisUtil.getHits(c.getContentId())))
				.toList();

		return ExploreResponse.builder()
				.contents(result).build();
	}

	// 컨텐츠 상세 정보 조회 + 조회수
	@Transactional
	public ContentDetailResponse getContentDetailWithCount(Long userId, Long contentId) {
		User user = findUserById(userId);
		Content content = findContentById(contentId);

		// 읽은 기록이 없을 때만 무료 횟수 차감 및 기록 생성
		if (!isReadContent(userId, contentId)) {
			user.incrementReadCount();
			readContentRepository.save(ReadContent.of(user, content, 0L, false));
		}

		redisUtil.updateHits(contentId, userId);
		return ContentDetailResponse.from(content, redisUtil.getHits(contentId));
	}

	// 타이틀로 컨텐츠 검색
	@Transactional
	public List<ContentResponse> search(Long userId, String keyword, int page) {
		String k = (keyword == null) ? "" : keyword.trim();
		if (k.isEmpty())
			return List.of();

		ContentLevel level = getUserContentLevel(userId);

		List<Content> searchResults = contentRepository.searchByTitle(
				level, k, PageRequest.of(page, 10));

		return searchResults.stream()
				.map(c -> ContentResponse.from(c, redisUtil.getHits(c.getContentId())))
				.toList();
	}


	// 읽은 글 상세
	public ReadContentDetailResponse getReadContentDetail(Long userId, Long contentId) {

		ContentDetailResponse contentDetail = getContentDetailWithCount(userId, contentId);

		return quizSolveRepository.findByUser_IdAndReadContent_Content_ContentId(userId, contentId)
			.map(solve -> {
				Quiz quiz = quizRepository.findById(solve.getQuizId())
					.orElseThrow(() -> new NotFoundException(ErrorMessage.QUIZ_NOT_FOUND));

				List<QuizChoiceResponse> choices = quizChoiceRepository.findByQuiz_QuizIdOrderByChoiceNoAsc(
						quiz.getQuizId())
					.stream()
					.map(QuizChoiceResponse::from)
					.toList();

				QuizChoice correct = quizChoiceRepository.findByQuiz_QuizIdAndIsCorrectTrue(quiz.getQuizId())
					.orElseThrow(() -> new NotFoundException(ErrorMessage.QUIZ_CORRECT_CHOICE_NOT_FOUND));

				SolvedQuizResponse solvedQuiz = SolvedQuizResponse.of(
					quiz.getQuizId(),
					contentId,
					quiz.getQuestion(),
					choices,
					solve.getSelectedNo(),
					correct.getChoiceNo(),
					solve.isAnswerCorrect(),
					solve.getSolvedAt()
				);

				return ReadContentDetailResponse.of(contentDetail, solvedQuiz);
			})
			.orElseGet(() -> {
				return ReadContentDetailResponse.of(contentDetail, null);
			});
	}

	//컨텐츠 읽기 권한 확인
	public ContentAccessResponse checkContentReadAccess(Long userId, Long contentId) {
		User user = findUserById(userId);
		int todayReadCount = user.getCountReadContent();

		if (isReadContent(userId, contentId)) {
			return ContentAccessResponse.ofReadable(user.getPoint());
		}

		int limit = user.isNewUserBonusPeriod()
			? ReadableContentLimitsInfo.IS_NEW_USER_LIMIT.getLimit()
			: ReadableContentLimitsInfo.EXIST_USER.getLimit();

		if (todayReadCount > limit) {
			return resolvePointOrAdResponse(user);
		}

		return ContentAccessResponse.ofReadable(user.getPoint());
	}

	//포인트 상태 파악 (부족 -> 광고 , 가능 -> 포인트 사용)
	private ContentAccessResponse resolvePointOrAdResponse(User user) {
		int currentUserPoint = user.getPoint();
		int needPoint = PointExperienceProvisionInformation.NEED_READ_CONTENT_POINT;

		if (currentUserPoint >= needPoint) {
			return ContentAccessResponse.ofUsePoint(currentUserPoint, needPoint);
		} else {
			int lackOfPoints = needPoint - currentUserPoint;
			return ContentAccessResponse.ofUseAd(
					currentUserPoint,
					needPoint,
					lackOfPoints,
					PointExperienceProvisionInformation.WATCH_AD_REWARDS_POINT
			);
		}
	}

	// 콘텐츠 다 읽고 나갈때 (체류 시간) * 프론트 에서 값을 넘겨주는 형식 - 완독 하면 포인트 주는 로직
	@Transactional
	public ReadStatusResponse updateReadStatus(Long userId, Long contentId,
		UpdateReadStatusRequest updateReadStatusRequest, boolean isFromMission) {

		ReadContent readContent = findReadContentById(userId, contentId);
		User user = findUserById(userId);

		if (readContent.isCompleted()) {
			return ReadStatusResponse.builder().isCompleted(true).build();
		}

		readContent.updateStatus(updateReadStatusRequest.staySeconds());

		if (readContent.isCompleted() && updateReadStatusRequest.isCompleted()) {
			Level previousLevel = user.getLevel();

			rewardReadContent(user, isFromMission);

			boolean isLevelUp = !previousLevel.equals(user.getLevel());
			LevelUpInfo levelUpInfo = isLevelUp ? LevelUpInfo.of(
				storageConfig.getProfileUrl(user.getProfileImgFileName()),
				user.getCharacterLevel().toString(),
				user.getCharacterLevel().getCharacterName()
			) : null;

			return ReadStatusResponse.builder()
				.isCompleted(true)
				.isLevelUp(isLevelUp)
				.levelUpInfo(levelUpInfo)
				.build();
		}

		return ReadStatusResponse.builder()
			.isCompleted(false)
			.isLevelUp(false)
			.build();
	}

	//완독 여부에 따른 포인트 지급 및 지급 여부 기록
	@Transactional
	public void rewardReadContent(User user, boolean isFromMission) {
		int rewardReadContentExp = PointExperienceProvisionInformation.COMPLETE_READ_CONTENT_EXP;

		user.addPointAndExp(0, rewardReadContentExp);

		RewardHistory rewardHistory = RewardHistory.create(user, 0, rewardReadContentExp,
			HistoryMessage.READ_THE_CONTENT);

		redisUtil.incrementMissionCount(user.getId(), MissionType.EXPLORE_READ);

		//미션 탭에서 들어온 경우 -> 홈 카운트 증가
		if (isFromMission) {
			redisUtil.incrementMissionCount(user.getId(), MissionType.HOME_READ);
		}

		rewardHistoryRepository.save(rewardHistory);
	}

	@Transactional
	public void purchaseContentByPoint(Long userId, Long contentId) {
		purchaseContentProcess(userId, contentId);
	}

	@Transactional
	public void watchAdAndRewardUnLockContent(Long userId, Long contentId) {
		User user = findUserById(userId);

		int watchAdRewardsPoint = PointExperienceProvisionInformation.WATCH_AD_REWARDS_POINT;

		user.addPointAndExp(watchAdRewardsPoint, 0);

		RewardHistory watchAdRewardPointHistory = RewardHistory.create(user, watchAdRewardsPoint, 0,
			HistoryMessage.WATCH_ADS_COMPLETED);

		rewardHistoryRepository.save(watchAdRewardPointHistory);

		purchaseContentProcess(userId, contentId);
	}

	// 공통 결제 로직 (포인트 차감 및 읽기 권한 부여)
	@Transactional
	public void purchaseContentProcess(Long userId, Long contentId) {
		User user = findUserById(userId);
		Content content = findContentById(contentId);

		if (isReadContent(userId, contentId)) {
			return;
		}

		int needPoint = PointExperienceProvisionInformation.NEED_READ_CONTENT_POINT;

		// 포인트 부족 체크
		if (user.getPoint() < needPoint) {
			throw new BadRequestException(ErrorMessage.NOT_ENOUGH_POINT);
		}

		user.addPointAndExp(-needPoint, 0);
		readContentRepository.save(ReadContent.of(user, content, 0L, false));
	}

	/**
	 * 컨텐츠 난이도 / 레벨
	 */

	@Transactional
	public DifficultyRecommendResponse requestRecommendContentLevel(Long userId) {

		DifficultyBasetime baseTime = findDifficultyBaseTimeByUserId(userId);

		User user = findUserById(userId);
		Level currentLevel = user.getLevel();
		LocalDateTime since = baseTime.getBaseTime();

		long totalCount = contentDifficultyEvaluationRepository.countByUserIdAfter(userId, since);

		// 20회 미만이면 모댤 표시 안함
		if (totalCount < DifficultyRecommendConfig.BASE_TOTAL_COUNT) {
			return DifficultyRecommendResponse.noDisplay();
		}

		long maintenanceCount = contentDifficultyEvaluationRepository.countByUserIdAndDifficultyAfter(
			userId, ContentDifficulty.MEDIUM, since);

		// 유지 기준(9회) 이상이면 모댤 표시 안함
		if (maintenanceCount >= DifficultyRecommendConfig.MAINTENANCE_REFERENCE) {
			return DifficultyRecommendResponse.noDisplay();
		}

		long hardCount = contentDifficultyEvaluationRepository.countByUserIdAndDifficultyAfter(
			userId, ContentDifficulty.HARD, since);
		long easyCount = contentDifficultyEvaluationRepository.countByUserIdAndDifficultyAfter(
			userId, ContentDifficulty.EASY, since);

		if (hardCount >= DifficultyRecommendConfig.DECREASE_THRESHOLD) {
			return RequestRecommendContentMessage.RECOMMEND_DECREASE_BEGINNER_MESSAGE
				.choiceRecommendDecrease(currentLevel)
				.map(recommend -> DifficultyRecommendResponse.display(DifficultyRecommend.DECREASE, recommend))
				.orElseGet(DifficultyRecommendResponse::noDisplay);
		}

		if (easyCount >= DifficultyRecommendConfig.INCREASE_THRESHOLD) {
			return RequestRecommendContentMessage.RECOMMEND_INCREASE_MESSAGE
				.choiceRecommendIncrease(currentLevel)
				.map(recommend -> DifficultyRecommendResponse.display(DifficultyRecommend.INCREASE, recommend))
				.orElseGet(DifficultyRecommendResponse::noDisplay);
		}

		return DifficultyRecommendResponse.noDisplay();
	}

	//컨텐츠 난이도 변경 선택
	@Transactional
	public void changeUserLevel(Long userId, Level level) {

		DifficultyBasetime baseTime = findDifficultyBaseTimeByUserId(userId);
		User user = findUserById(userId);

		//중복 클릭 방지
		if (baseTime.getBaseTime().isAfter(LocalDateTime.now().minusSeconds(5))) {
			throw new BadRequestException(ErrorMessage.ALREADY_LEVEL_CHANGE);
		}

		//레벨 변경
		user.changeLevel(level);
		baseTime.updateBaseTime(LocalDateTime.now());
	}

	//컨텐츠 난이도 평가 (퀴즈 풀이 후 모댤)
	@Transactional
	public void contentDifficultyAssessment(Long userId, Long contentId, ContentDifficulty difficulty) {

		ReadContent readContent = findReadContentByContentIdAndCheckContentDifficulty(userId, contentId);

		ContentDifficultyEvaluation evaluationResult = ContentDifficultyEvaluation.create(
			readContent,
			difficulty
		);

		contentDifficultyEvaluationRepository.save(evaluationResult);

	}

	//ReadContent + 난이도 평가 여부 조회
	public ReadContent findReadContentByContentIdAndCheckContentDifficulty(Long userId, Long contentId) {

		ReadContent readContent = readContentRepository.findByUser_IdAndContent_ContentId(userId, contentId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.READ_RECORD_NOT_FOUND));

		if (contentDifficultyEvaluationRepository.existsByReadContent_ReadContentId(readContent.getReadContentId())) {
			throw new ConflictException(ErrorMessage.CONTENT_ALREADY_EVALUATED);
		}

		return readContent;
	}

	private User findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND));
	}

	private DifficultyBasetime findDifficultyBaseTimeByUserId(Long userId) {
		return difficultyBasetimeRepository.findByUserId(userId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.CONTENT_DIFFICULTY_ASSESSMENT_NOT_FOUND));
	}

	//컨텐츠 읽은 여부 체크
	private boolean isReadContent(Long userId, Long contentId) {
		return readContentRepository.existsByUser_IdAndContent_ContentId(userId, contentId);
	}

	//Content 조회
	private Content findContentById(Long contentId) {
		return contentRepository.findById(contentId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.CONTENT_NOT_FOUND));
	}

	//읽은 컨텐츠 조회
	private ReadContent findReadContentById(Long userId, Long contentId) {
		return readContentRepository.findByUser_IdAndContent_ContentId(userId, contentId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.READ_RECORD_NOT_FOUND));
	}

	//객체 타입 일치
	private ContentLevel getUserContentLevel(Long userId) {
		return userRepository.findLevelByUserId(userId)
			.map(obj -> {
				try {
					return ContentLevel.valueOf(obj.toString());
				} catch (Exception e) {
					return ContentLevel.BEGINNER; // 매핑 실패 시 기본값
				}
			})
			.orElse(ContentLevel.BEGINNER);
	}
}
