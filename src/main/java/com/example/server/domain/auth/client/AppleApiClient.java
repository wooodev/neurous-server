package com.example.server.domain.auth.client;

import java.util.Base64;

import com.example.server.domain.auth.service.AppleOAuthTokenService;
import org.springframework.stereotype.Component;

import com.example.server.domain.auth.dto.AppleUserInfo;
import com.example.server.domain.auth.dto.OAuthUserInfo;
import com.example.server.domain.auth.enums.OAuthProvider;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NeurousException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppleApiClient implements OAuthClient {

	private final ObjectMapper objectMapper;
	private final AppleOAuthTokenService appleOAuthTokenService;

	@Override
	public OAuthProvider getProvider() {
		return OAuthProvider.APPLE;
	}

	@Override
	public OAuthUserInfo getUserInfo(String identityToken) {
		try {

			String payload = getUnverifiedPayload(identityToken);
			AppleUserInfo userInfo = objectMapper.readValue(payload, AppleUserInfo.class);

			validateUserInfo(userInfo);

			return userInfo;
		} catch (JsonProcessingException e) {
			log.error("Apple UserInfo 파싱 실패: {}", e.getMessage());
			throw new NeurousException(ErrorMessage.TOKEN_PARSE_FAILED);
		} catch (Exception e) {
			log.error("애플 로그인 처리 중 오류 발생: {}", e.getMessage());
			throw new NeurousException(ErrorMessage.OAUTH2_APPLE_API_ERROR);
		}
	}

	/**
	 * 서명 검증 없이 페이로드만 추출 (내부용)
	 */
	private String getUnverifiedPayload(String identityToken) {
		String[] parts = identityToken.split("\\.");
		if (parts.length < 2) {
			throw new NeurousException(ErrorMessage.INVALID_JWT_STRUCTURE);
		}
		return new String(Base64.getUrlDecoder().decode(parts[1]));
	}

	private void validateUserInfo(AppleUserInfo userInfo) {
		if (userInfo.getSub() == null || userInfo.getSub().isBlank()) {
			log.error("Apple Identity Token에 고유 식별자(sub)가 없습니다.");
			throw new NeurousException(ErrorMessage.INVALID_JWT_STRUCTURE);
		}
	}

	private Claims verifyToken(String identityToken) {
		return Jwts.parser()
			.build()
			.parseClaimsJwt(identityToken)
			.getBody();
	}

	@Override
	public void unlink(String refreshToken) {
		appleOAuthTokenService.revokeByRefreshToken(refreshToken);
	}
}
