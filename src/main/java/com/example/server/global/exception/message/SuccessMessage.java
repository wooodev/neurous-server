package com.example.server.global.exception.message;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessMessage {

	LOGIN_SUCCESS(HttpStatus.OK.value(), "로그인이 완료되었습니다"),
	LOGOUT_SUCCESS(HttpStatus.OK.value(), "로그아웃이 완료되었습니다"),
	SIGN_UP_SUCCESS(HttpStatus.OK.value(), "회원가입이 완료되었습니다 "),
	LOAD_SUCCESS(HttpStatus.OK.value(), "조회가 완료되었습니다"),
	UPDATE_SUCCESS(HttpStatus.OK.value(), "수정이 완료되었습니다"),

	QUIZ_SUBMIT_SUCCESS(HttpStatus.OK.value(), "퀴즈 답안이 성공적으로 제출되었습니다."),
	GET_QUIZ_SUCCESS(HttpStatus.OK.value(), "퀴즈를 성공적으로 가져왔습니다"),

	LOAD_CONTENT_EXPLORE_SUCCESS(HttpStatus.OK.value(), "컨텐츠 탐색 페이지 조회가 완료되었습니다"),
	LOAD_CONTENT_EXPLORE_BY_CATEGORY_SUCCESS(HttpStatus.OK.value(), "카테고리 기반 탐색 페이지 조회가 완료되었습니다"),
	LOAD_CONTENT_DETAIL_SUCCESS(HttpStatus.OK.value(), "컨텐츠 상세 조회가 완료되었습니다"),
	PURCHASE_CONTENT_SUCCESS(HttpStatus.NO_CONTENT.value(), "컨텐츠 구매가 완료되었습니다"),
	REWARD_POINT_BY_AD_PURCHASE_CONTENT_SUCCESS(HttpStatus.NO_CONTENT.value(), "광고 시청 포인트 지급 과 컨텐츠 구매가 완료되었습니다"),

	// Content
	LOAD_READ_CONTENT_DETAIL_SUCCESS(HttpStatus.OK.value(), "읽은 컨텐츠 상세 조회가 완료되었습니다"),
	SEARCH_CONTENT_SUCCESS(HttpStatus.OK.value(), "컨텐츠 검색이 완료되었습니다"),
	LOAD_RECENT_SEARCH_SUCCESS(HttpStatus.OK.value(), "최근 검색어 조회가 완료되었습니다"),
	CHECK_CONTENT_ACCESS_SUCCESS(HttpStatus.OK.value(), "컨텐츠 읽기 권한이 성공적으로 확인되었습니다"),
	UPDATE_READ_STATUS_SUCCESS(HttpStatus.OK.value(), "컨텐츠 완독 여부 상태를 업데이트 하였습니다"),

	UPDATE_SUCCESS_NOTIFICATION_SETTING(HttpStatus.OK.value(), "알림 설정이 완료되었습니다"),

	UNLOCK_CONTENT_BY_AD_SUCCESS(HttpStatus.NO_CONTENT.value(), "광고 시청 후 컨텐츠가 해금되었습니다"),

	RECOMMEND_CONTENT_DIFFICULTY_SUCCESS(HttpStatus.OK.value(), "컨텐츠 난이도 추천 결과 조회가 완료되었습니다"),

	CHANGE_LEVEL_SUCCESS(HttpStatus.OK.value(), "유저 학습 레벨이 변경되었습니다"),
	EVALUATE_CONTENT_DIFFICULTY_SUCCESS(HttpStatus.OK.value(), "컨텐츠 난이도 평가가 완료되었습니다"),

	LOAD_SUCESS_MISSION_CONTENTS(HttpStatus.OK.value(), "미션 컨텐츠 조회가 완료되었습니다"),

	LOAD_SUCCESS_CHARACTER_PAGE(HttpStatus.OK.value(), "캐릭터 페이지 정보를 성공적으로 불러왔습니다"),
	LOAD_SUCCESS_LEVEL_STANDARD(HttpStatus.OK.value(), "레벨 기준 정보를 성공적으로 불러왔습니다"),
	LOAD_SUCCESS_REWARD_STANDARD(HttpStatus.OK.value(), "보상 기준 정보를 성공적으로 불러왔습니다"),
	LOAD_SUCCESS_REWARD_HISTORY(HttpStatus.OK.value(), "포인트 / 경험치 내역을 성공적으로 불러왔습니다"),

	GET_SUCCESS_NOTIFICATION_LIST(HttpStatus.OK.value(), "알림 정보를 성공적으로 불러왔습니다"),
	UPDATE_SUCCESS_NOTIFICATION_READ(HttpStatus.OK.value(), " 알림 읽은 여부를 업데이트 하였습니다"),

	//201
	ACCESS_TOKEN_REISSUE_SUCCESS(HttpStatus.CREATED.value(), "액세스 토큰 재발급이 완료되었습니다."),
	WITHDRAW_SUCCESS(HttpStatus.OK.value(), "회원 탈퇴가 완료되었습니다");

	private final int status;
	private final String message;

	public String formatMessage(Object... args) {
		return String.format(this.message, args);
	}

}
