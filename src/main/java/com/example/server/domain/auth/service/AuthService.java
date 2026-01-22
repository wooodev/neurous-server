package com.example.server.domain.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.server.domain.auth.client.OAuthClient;
import com.example.server.domain.auth.client.OAuthClientResolver;
import com.example.server.domain.auth.controller.dto.response.LoginResponse;
import com.example.server.domain.auth.controller.dto.response.RefreshResponse;
import com.example.server.domain.auth.dto.OAuthUserInfo;
import com.example.server.domain.auth.dto.UserInfo;
import com.example.server.domain.auth.entity.TokenManager;
import com.example.server.domain.auth.enums.OAuthProvider;
import com.example.server.domain.auth.repository.RefreshTokenRepository;
import com.example.server.domain.user.entity.User;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NeurousException;
import com.example.server.global.exception.model.NotFoundException;
import com.example.server.global.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	//리프레시 토큰의 최대 유효 기간  * 30일
	private static final int REFRESH_TOKEN_VALIDITY_DAYS = 30;
	//재발급 * 14일
	private static final int REFRESH_TOKEN_RENEWAL_THRESHOLD_DAYS = 14;

	private final OAuthClientResolver oAuthClientResolver;
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;

	//로그인
	@Transactional
	public LoginResponse login(OAuthProvider provider, String oauthAccessToken) {
		OAuthClient oauthClient = oAuthClientResolver.getClient(provider);
		OAuthUserInfo oauthUserInfo = oauthClient.getUserInfo(oauthAccessToken);

		User user = findOrCreateUser(provider, oauthUserInfo);

		String accessToken = jwtTokenProvider.generateToken(String.valueOf(user.getId()), user.getEmail());
		String refreshToken = jwtTokenProvider.generateRefreshToken(String.valueOf(user.getId()), user.getEmail());

		saveRefreshToken(user, refreshToken);

		UserInfo userInfo = UserInfo.from(user);

		boolean isSignUpComplete = user.isSignUpComplete();

		user.updateLastLoginAt(LocalDateTime.now());

		return LoginResponse.of(accessToken, refreshToken, userInfo, !isSignUpComplete);
	}

	@Transactional
	public RefreshResponse refresh(String refreshTokenValue) {

		//가짜 토큰 판별 * 걸러내기
		if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
			throw new NeurousException(ErrorMessage.INVALID_TOKEN);
		}

		// DB 토큰 존재 여부 확인
		TokenManager refreshToken = findRefreshToken(refreshTokenValue);

		//비즈니스 로직 만료 야부 확인
		validateRefreshToken(refreshToken);

		User user = refreshToken.getUser();

		String newAccessToken = jwtTokenProvider.generateToken(String.valueOf(user.getId()), user.getEmail());

		if (isRefreshTokenExpired(refreshToken)) {
			String newRefreshToken = jwtTokenProvider.generateRefreshToken(String.valueOf(user.getId()),
				user.getEmail());
			updateRefreshToken(refreshToken, newRefreshToken);
			return RefreshResponse.of(newAccessToken, newRefreshToken);
		}

		return RefreshResponse.of(newAccessToken, refreshToken.getTokenValue());
	}

	@Transactional
	public void logout(Long currentUserId) {
		User user = userRepository.findById(currentUserId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND));

		refreshTokenRepository.findByUser(user)
			.ifPresent(refreshTokenRepository::delete);

	}

	private User findOrCreateUser(OAuthProvider provider, OAuthUserInfo oauthUserInfo) {
		return userRepository.findByProviderAndProviderId(provider, oauthUserInfo.getProviderId())
			.map(user -> {
				user.updateSocialInfo(oauthUserInfo.getName(), oauthUserInfo.getEmail());
				return user;
			})
			.orElseGet(() -> createUser(provider, oauthUserInfo));
	}

	//유저 생성 (*추가정보 제외)
	private User createUser(OAuthProvider provider, OAuthUserInfo oauthUserInfo) {
		User user = User.create(provider, oauthUserInfo);
		return userRepository.save(user);
	}

	//로그인성공시 RefreshToken 저장 / 갱신
	private void saveRefreshToken(User user, String tokenValue) {

		LocalDateTime expiredAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);

		refreshTokenRepository.findByUser(user)
			.ifPresentOrElse(
				token -> updateExistingToken(token, tokenValue, expiredAt),
				() -> createNewToken(user, tokenValue, expiredAt)
			);
	}

	//기존 Refresh Token 갱신 로직
	private void updateExistingToken(TokenManager token, String tokenValue, LocalDateTime expiredAt) {
		token.updateToken(tokenValue, expiredAt);
	}

	//최초 로그인
	private void createNewToken(User user, String tokenValue, LocalDateTime expiredAt) {
		TokenManager newToken = TokenManager.of(user, tokenValue, expiredAt);
		refreshTokenRepository.save(newToken);
	}

	private TokenManager findRefreshToken(String tokenValue) {
		return refreshTokenRepository.findByTokenValue(tokenValue)
			.orElseThrow(() -> new NeurousException(ErrorMessage.INVALID_TOKEN));
	}

	//토큰 유효성 검증
	private void validateRefreshToken(TokenManager refreshToken) {
		if (refreshToken.isInvalid()) {
			refreshTokenRepository.delete(refreshToken);
			log.info("유효하지 않은 Refresh Token 삭제: id={}, 사유={}",
				refreshToken.getId(),
				refreshToken.isExpired() ? "기간만료" : "폐기됨");

			throw new NeurousException(ErrorMessage.EXPIRED_TOKEN);
		}
	}

	private boolean isRefreshTokenExpired(TokenManager refreshToken) {
		LocalDateTime threshold = LocalDateTime.now().plusDays(REFRESH_TOKEN_RENEWAL_THRESHOLD_DAYS);
		return refreshToken.getExpiredAt().isBefore(threshold);
	}

	private void updateRefreshToken(TokenManager refreshToken, String newTokenValue) {
		LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);
		refreshToken.updateToken(newTokenValue, newExpiresAt);
	}

	private long getRemainingDay(TokenManager refreshToken) {
		return java.time.Duration.between(LocalDateTime.now(), refreshToken.getExpiredAt()).toDays();
	}

}


