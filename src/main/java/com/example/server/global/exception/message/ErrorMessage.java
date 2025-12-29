package com.example.server.global.exception.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage {

	//공통에러
	NS_ENUM_VALUE_BAD_REQUEST(400, "NS1001", "요청한 값이 유효하지 않습니다"),
	NS_VALIDATION_MISSING(400, "NS1002", "요청 값이 비어 있습니다"),
	NS_VALIDATION_NULL_OR_BLANK(400, "NS1003", "필수 요청 값이 누락되었습니다."),
	NS_VALIDATION_LENGTH_EXCEEDED(400, "NS1004", "요청 값이 길이를 초과했습니다."),
	NS_NO_FORBIDDEN(403, "NS1005", "권한이 없습니다. "),
	NS_TEMP_TOKEN_EMPTY(404, "NS1006", "TempToken 값은 null이거나 비어있을 수 없습니다."),
	NS_UNAUTHORIZED(401, "NS1007", "권한이 없습니다"),

	//사용자 관련 에러 & 인증 / 인가
	INVALID_TOKEN(401, "AUTH1001", "유효하지 않은 토큰입니다"),
	EXPIRED_TOKEN(401, "AUTH1002", "만료된 토큰입니다"),
	ACCESS_DENIED(403, "AUTH1003", "접근이 거부되었습니다"),
	SESSION_EXPIRED(401, "AUTH1004", "세션이 만료되었습니다. 다시 로그인해주세요"),
	UNSUPPORTED_LOGIN_METHOD(400, "AUTH1005", "지원하지않는 로그인 방식입니다"),
	TOKEN_DECODE_FAILED(400, "AUTH1006", "토큰 디코딩에 실패했습니다"),
	TOKEN_PARSE_FAILED(400, "AUTH1006", "토큰 파싱에 실패했습니다"),
	//JWT
	INVALID_JWT_STRUCTURE(400, "AUTH1007", "JWT 구조가 올바르지 않습니다"),
	// OAuth2 관련
	OAUTH2_PROVIDER_MISSING(400, "OAUTH2006", "로그인 방식이 전달되지 않았습니다"),
	OAUTH2_KAKAO_API_ERROR(500, "OAUTH2007", "카카오 API 호출에 실패했습니다."),
	OAUTH2_GOOGLE_API_ERROR(500, "OAUTH2008", "구글 API 호출에 실패했습니다."),
	OAUTH2_APPLE_API_ERROR(500, "OAUTH2009", "애플 API 호출에 실패했습니다."),
	OAUTH2_NAVER_API_ERROR(500, "OAUTH2010", "네이버 API 호출에 실패했습니다."),

	//User 관련
	USER_NOT_FOUND(404, "USER3001", "유저를 찾을 수 없습니다"),
	USER_INVALID_INTEREST_COUNT(400, "USER3002", "관심분야는 3가지 모두 선택해야합니다"),
	USER_DUPLICATED_INTEREST(400, "USER3003", "중복된 관심분야 항목이 있습니다"),

	//컨텐츠 관련
	NOT_FOUND_LEVEL_DESCRIPTION(404, "CNT4001", "선택한 레벨에 대한 정보가 없습니다"),
	CONTENT_NOT_FOUND(404, "CNT4002", "존재하지 않는 컨텐츠입니다."),
	CONTENT_INTEREST_NOT_SET(400, "CNT4003", "관심분야가 설정되지 않았습니다."),
	CONTENT_TODAY_NOT_AVAILABLE(404, "CNT4004", "추천 가능한 콘텐츠가 없습니다."),
	CONTENT_ALREADY_EVALUATED(409, "CNT4005", "이미 평가한 컨텐츠입니다."),
	CONTENT_ALREADY_READ(409, "CNT4006", "이미 읽음 처리된 컨텐츠입니다."),

	// 퀴즈 관련
	QUIZ_NOT_FOUND_FOR_CONTENT_LEVEL(404, "QUIZ5001", "해당 컨텐츠에 난이도별 퀴즈가 없습니다."),
	QUIZ_NOT_FOUND(404, "QUIZ5002", "존재하지 않는 퀴즈입니다."),
	QUIZ_INVALID_CHOICE(400, "QUIZ5003", "존재하지 않는 선택지입니다."),
	QUIZ_CORRECT_ANSWER_NOT_CONFIGURED(500, "QUIZ5004", "퀴즈 정답 데이터가 설정되지 않았습니다."),
	QUIZ_SOLVE_NOT_FOUND(404, "QUIZ5005", "해당 콘텐츠에 대한 퀴즈 풀이 기록이 없습니다."),
	QUIZ_CORRECT_CHOICE_NOT_FOUND(404, "QUIZ5006", "정답 선택지가 설정되지 않았습니다."),
	QUIZ_ALREADY_SOLVED(409, "QUIZ5007", "이미 퀴즈를 제출했습니다."),

	// 서버 에러
	INTERNAL_SERVER_ERROR(500, "INT5000", "서버 내부 오류가 발생했습니다.");


	private final int status;
	private final String code;
	private final String message;
}
