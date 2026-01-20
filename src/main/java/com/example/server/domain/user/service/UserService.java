package com.example.server.domain.user.service;

import com.example.server.domain.auth.client.OAuthClient;
import com.example.server.domain.auth.client.OAuthClientResolver;
import com.example.server.domain.auth.enums.OAuthProvider;
import com.example.server.domain.auth.repository.RefreshTokenRepository;
import com.example.server.domain.auth.service.AppleOAuthTokenService;
import com.example.server.domain.user.controller.dto.request.WithdrawRequest;
import com.example.server.global.exception.model.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.server.domain.user.controller.dto.request.UpdateInterestsRequest;
import com.example.server.domain.user.controller.dto.response.UserInterestsResponse;
import com.example.server.domain.user.entity.User;
import com.example.server.domain.user.entity.vo.Level;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final OAuthClientResolver oAuthClientResolver;
	private final AppleOAuthTokenService appleOAuthTokenService;

	//관심분야 설정 (온보딩)
	@Transactional
	public UserInterestsResponse updateInterest(Long userId, UpdateInterestsRequest updateInterestsRequest) {
		User user = findByUserId(userId);
		user.updateInterests(updateInterestsRequest.interests());
		return UserInterestsResponse.from(user.getInterests());
	}

	// 레벨 변경 (초급 / 중급 / 고급) (온보딩)
	@Transactional
	public void updateLevel(Long userId, Level level) {
		User user = findByUserId(userId);
		user.changeLevel(level);
		user.completeSignUp();
	}

	public User findByUserId(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND)
			);
	}

	@Transactional
	public void withdraw(Long userId, WithdrawRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND));

		if (request != null && request.unlinkSocial()) {
			unlinkSocial(user, request);
		}

		refreshTokenRepository.deleteByUser(user);

		user.withdrawAndAnonymize();
	}

	private void unlinkSocial(User user, WithdrawRequest request) {
		OAuthProvider provider = user.getProvider();

		try {
			if (provider == OAuthProvider.APPLE) {
				String refreshToken = user.getAppleRefreshToken();

				if ((refreshToken == null || refreshToken.isBlank())
						&& request.appleAuthorizationCode() != null
						&& !request.appleAuthorizationCode().isBlank()) {
					String newRefreshToken =
							appleOAuthTokenService.exchangeAuthorizationCodeForRefreshToken(request.appleAuthorizationCode());
					user.updateAppleRefreshToken(newRefreshToken);
					refreshToken = user.getAppleRefreshToken();
				}

				if (refreshToken == null || refreshToken.isBlank()) {
					throw new BadRequestException(ErrorMessage.NS_ENUM_VALUE_BAD_REQUEST);
				}

				oAuthClientResolver.getClient(OAuthProvider.APPLE).unlink(refreshToken);
				return;
			}

			// KAKAO / NAVER / GOOGLE
			if (request.providerAccessToken() == null || request.providerAccessToken().isBlank()) {
				throw new BadRequestException(ErrorMessage.NS_ENUM_VALUE_BAD_REQUEST);
			}

			OAuthClient client = oAuthClientResolver.getClient(provider);
			client.unlink(request.providerAccessToken());

		} catch (RuntimeException e) {
			log.warn("[withdraw] social unlink failed provider={}", provider, e);
			throw e;
		}
	}

}
