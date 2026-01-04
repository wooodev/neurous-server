package com.example.server.domain.user.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.example.server.domain.auth.dto.OAuthUserInfo;
import com.example.server.domain.auth.enums.OAuthProvider;
import com.example.server.domain.content.entity.vo.ContentLevel;
import com.example.server.domain.user.entity.vo.CharacterLevel;
import com.example.server.domain.user.entity.vo.Level;
import com.example.server.domain.user.entity.vo.Priority;
import com.example.server.domain.user.entity.vo.UserField;
import com.example.server.domain.user.entity.vo.UserInterest;
import com.example.server.domain.user.entity.vo.UserStatus;
import com.example.server.domain.user.entity.vo.UserType;
import com.example.server.global.domain.BaseTimeEntity;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.BadRequestException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseTimeEntity {

	@Id
	@Column(name = "user_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name; //*소셜로그인 값

	//소셜로그인
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OAuthProvider provider;

	@Column(nullable = false)
	private String providerId;

	@Builder.Default
	@Column(name = "profile_img_file_name")
	private String profileImgFileName = "lv1_profile.png"; // 기본값 설정

	@Column(unique = true, length = 50)
	@Email
	private String email; //*소셜로그인값

	@Builder.Default
	@Enumerated(EnumType.STRING)
	private UserStatus status = UserStatus.NORMAL;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 25)
	private UserType userType = UserType.USER;

	//흥미
	@Builder.Default
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserInterest> interests = new ArrayList<>();

	//순위 선택 * 수
	@Column(nullable = false, name = "count_interests")
	private int countInterests;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Level level = Level.BEGINNER; //기본값 : 초급 (컨텐츠 반영 레벨)

	@Builder.Default
	@Column(nullable = false, name = "sign_up_complete", columnDefinition = "TINYINT(1)")
	private boolean signUpComplete = false; //회원가입 이후 추가 정보까지 입력 여부

	@Builder.Default
	@Column(nullable = false, name = "notification_status", columnDefinition = "TINYINT(1)")
	private boolean notificationStatus = false; //알람 여부 미설정

	@Builder.Default
	@Column(nullable = false)
	private int point = 0; //현재 보유 포인트

	@Builder.Default
	@Column(nullable = false)
	private int exp = 0; //현재 보유 경험치

	@Builder.Default
	@Column(nullable = false)
	private int countReadContent = 0; //읽은 콘텐츠 개수

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CharacterLevel characterLevel = CharacterLevel.LEVEL_1;

	@Column(name = "last_read_date")
	private LocalDate lastReadDate;

	@Builder.Default
	@Column(nullable = false, name = "attendance_count")
	private int attendanceCount = 0;

	@Builder.Default
	@Column(nullable = false)
	private LocalDateTime lastLoginAt = LocalDateTime.now();

	@Builder.Default
	@Column(nullable = false, columnDefinition = "TINYINT(1)")
	private boolean pendingModuleLevelUp = false;

	//알림 여부 변경
	public void toggleNotification() {
		this.notificationStatus = !this.notificationStatus;
	}

	public static User create(OAuthProvider provider, OAuthUserInfo oauthUserInfo) {
		return User.builder()
			.name(oauthUserInfo.getName())
			.provider(provider)
			.providerId(oauthUserInfo.getProviderId())
			.email(oauthUserInfo.getEmail())
			.build();
	}

	public void updateSocialInfo(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public void updateLastLoginAt(LocalDateTime lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}

	public boolean isSignUpComplete() {
		return signUpComplete;
	}

	public void completeSignUp() {
		this.signUpComplete = true;
	}

	public void updateInterests(List<UserField> fields) {

		//최소 1개에서 ~ 3개
		if (fields == null || fields.isEmpty() || fields.size() > 3) {
			throw new BadRequestException(ErrorMessage.USER_INVALID_INTEREST_COUNT);
		}
		if (fields.stream().distinct().count() != fields.size()) {
			throw new BadRequestException(ErrorMessage.USER_DUPLICATED_INTEREST);
		}

		this.interests.clear();

		for (int i = 0; i < fields.size(); i++) {
			this.interests.add(
				UserInterest.of(
					this,
					fields.get(i),
					Priority.fromIndex(i)
				)
			);
		}
		//선택한 개수 업데이트 ( 미션 컨텐츠 제공에서 사용)
		this.countInterests = fields.size();
	}

	//레벨 변경
	public void changeLevel(Level level) {
		this.level = level;
	}

	public void changeCharacterLevel(CharacterLevel level) {
		this.characterLevel = level;
	}

	//신규 가입 인지 아닌지
	public boolean isNewUserBonusPeriod() {
		if (this.getCreatedAt() == null)
			return true;

		LocalDate signUpDate = this.getCreatedAt().toLocalDate();
		LocalDate today = LocalDate.now();

		long daysBetween = ChronoUnit.DAYS.between(signUpDate, today);
		return daysBetween >= 0 && daysBetween <= 2;
	}

	//포인트 * 경험치 총 증가
	public boolean addPointAndExp(int point, int exp) {
		this.point += point;
		this.exp += exp;

		CharacterLevel nextLevel = CharacterLevel.getLevelByExp(this.exp);

		if (this.characterLevel != nextLevel) {
			this.characterLevel = nextLevel;
			updateProfileImgByLevel();
			this.pendingModuleLevelUp = true;
			return true;
		}
		return false; //레벨업 미발생
	}

	//모댤 확인 완료 처리 (캐릭터 페이지 진입 시 호출)
	public void completeLevelUpModuleDisplayInCharacterPage() {
		this.pendingModuleLevelUp = false;
	}

	// 현재 레벨 내에서의 진척도 퍼센트 (정수형 0~100)
	public int calculateLevelProgress() {
		CharacterLevel currentLevel = this.characterLevel;
		CharacterLevel nextLevel = currentLevel.getNextLevel();

		if (currentLevel == nextLevel)
			return 100;

		int range = nextLevel.getThreshold() - currentLevel.getThreshold();

		int currentProgress = this.exp - currentLevel.getThreshold();

		int progressPercent = (currentProgress * 100) / range;

		return Math.min(100, Math.max(0, progressPercent));
	}

	//프로필 사진 번경 (레벨에 따라)
	private void updateProfileImgByLevel() {
		this.profileImgFileName = switch (this.characterLevel) {
			case LEVEL_1 -> ProfileImgFileName.LV1_PROFILE_IMG_FILE_NAME;
			case LEVEL_2 -> ProfileImgFileName.LV2_PROFILE_IMG_FILE_NAME;
			case LEVEL_3 -> ProfileImgFileName.LV3_PROFILE_IMG_FILE_NAME;
			case LEVEL_4 -> ProfileImgFileName.LV4_PROFILE_IMG_FILE_NAME;
			case LEVEL_5 -> ProfileImgFileName.LV5_PROFILE_IMG_FILE_NAME;
			default -> this.profileImgFileName; // 예외 케이스 대비
		};
	}

	//읽은 콘텐츠
	public void syncReadCount() {
		LocalDate today = LocalDate.now();

		// 마지막 읽은 날짜가 오늘이 아니면 카운트 리셋 및 날짜 갱신
		if (this.lastReadDate == null || !this.lastReadDate.isEqual(today)) {
			this.countReadContent = 0; // 초기화
			this.lastReadDate = today; // 오늘 날짜로 업데이트
		}
	}

	//읽은 콘텐츠 수 증가
	public void incrementReadCount() {
		this.countReadContent++;
	}

	public void updateAttendanceForOneWeek(LocalDateTime todayLoginDateTime) {
		if (this.lastLoginAt == null) {
			this.attendanceCount = 1;
			this.lastLoginAt = todayLoginDateTime;
			return;
		}

		LocalDate lastDate = this.lastLoginAt.toLocalDate();
		LocalDate currentDate = todayLoginDateTime.toLocalDate();
		long gap = ChronoUnit.DAYS.between(lastDate, currentDate);

		if (gap == 1) {
			this.attendanceCount++;
		} else if (gap > 1) {
			this.attendanceCount = 1;
		}
		this.lastLoginAt = todayLoginDateTime;
	}

	// User.java 내부 추가

	/**
	 * 유저의 현재 숙련도(Level)를 퀴즈 조회를 위한 ContentLevel로 변환합니다.
	 */
	public ContentLevel getContentLevel() {
		if (this.level == null)
			return ContentLevel.BEGINNER;
		
		try {
			return ContentLevel.valueOf(this.level.name());
		} catch (IllegalArgumentException e) {
			return ContentLevel.BEGINNER; // 매핑 실패 시 기본값
		}
	}

}
