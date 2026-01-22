package com.example.server.domain.auth.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.server.domain.auth.dto.KakaoUserInfo;
import com.example.server.domain.auth.dto.OAuthUserInfo;
import com.example.server.domain.auth.enums.OAuthProvider;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NeurousException;
import com.example.server.global.security.oauth.OAuthProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class KakaoApiClient implements OAuthClient {

	private final RestTemplate restTemplate;
	private final OAuthProperties oAuthProperties;

	@Override
	public OAuthProvider getProvider() {
		return OAuthProvider.KAKAO;
	}

	@Override
	public OAuthUserInfo getUserInfo(String accessToken) {
		try {
			return callKakaoUserInfoApi(accessToken);
		} catch (Exception e) {
			log.error("카카오 사용자 정보 조회 중 오류 발생", e);
			throw new NeurousException(ErrorMessage.OAUTH2_KAKAO_API_ERROR);
		}
	}

	private KakaoUserInfo callKakaoUserInfoApi(String accessToken) {
		String url = oAuthProperties.getKakao().getUserInfoUrl();
		HttpHeaders headers = createAuthHeaders(accessToken);
		HttpEntity<Void> request = new HttpEntity<>(headers);

		ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(url, HttpMethod.GET, request,
				KakaoUserInfo.class);

		KakaoUserInfo body = response.getBody();
		if (body == null) {
			log.error("카카오 API 응답 바디가 비어있습니다.");
			throw new NeurousException(ErrorMessage.OAUTH2_KAKAO_API_ERROR);
		}
		return body;
	}

	private HttpHeaders createAuthHeaders(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);

		headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		return headers;
	}

	@Override
	public void unlink(String accessToken) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);

			HttpEntity<Void> request = new HttpEntity<>(headers);

			restTemplate.exchange(
					oAuthProperties.getKakao().getUnlinkUrl(),
					HttpMethod.POST,
					request,
					String.class
			);
		} catch (RestClientException e) {
			throw new NeurousException(ErrorMessage.OAUTH2_KAKAO_API_ERROR);
		}
	}
}