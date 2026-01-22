package com.example.server.domain.auth.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.server.domain.auth.dto.NaverUserInfo;
import com.example.server.domain.auth.dto.OAuthUserInfo;
import com.example.server.domain.auth.enums.OAuthProvider;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NeurousException;
import com.example.server.global.security.oauth.OAuthProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RequiredArgsConstructor
@Component
public class NaverApiClient implements OAuthClient {

	private final RestTemplate restTemplate;
	private final OAuthProperties oAuthProperties;

	@Override
	public OAuthProvider getProvider() {
		return OAuthProvider.NAVER;
	}

	@Override
	public OAuthUserInfo getUserInfo(String accessToken) {
		try {
			return callNaverUserInfoApi(accessToken);
		} catch (Exception e) {
			log.error("네이버 사용자 정보 조회 중 오류 발생", e);
			throw new NeurousException(ErrorMessage.OAUTH2_NAVER_API_ERROR);
		}
	}

	private NaverUserInfo callNaverUserInfoApi(String accessToken) {
		String url = oAuthProperties.getNaver().getUserInfoUrl();
		HttpHeaders headers = createAuthHeaders(accessToken);
		HttpEntity<Void> request = new HttpEntity<>(headers);

		ResponseEntity<NaverUserInfo> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				request,
				NaverUserInfo.class
		);

		NaverUserInfo body = response.getBody();

		if (body == null) {
			throw new NeurousException(ErrorMessage.OAUTH2_NAVER_API_ERROR);
		}

		if (!"00".equals(body.getResultCode())) {
			log.error("네이버 API 호출 실패: {}", body.getMessage());
			throw new NeurousException(ErrorMessage.OAUTH2_NAVER_API_ERROR);
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
			String url = UriComponentsBuilder
					.fromHttpUrl(oAuthProperties.getNaver().getTokenUrl())
					.queryParam("grant_type", "delete")
					.queryParam("client_id", oAuthProperties.getNaver().getClientId())
					.queryParam("client_secret", oAuthProperties.getNaver().getClientSecret())
					.queryParam("access_token", accessToken)
					.queryParam("service_provider", "NAVER")
					.toUriString();

			restTemplate.getForEntity(url, String.class);
		} catch (RestClientException e) {
			throw new NeurousException(ErrorMessage.OAUTH2_NAVER_API_ERROR);
		}
	}
}