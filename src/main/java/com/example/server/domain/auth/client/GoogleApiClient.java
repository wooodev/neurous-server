package com.example.server.domain.auth.client;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.server.domain.auth.dto.GoogleUserInfo;
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
public class GoogleApiClient implements OAuthClient {

	private final RestTemplate restTemplate;
	private final OAuthProperties oAuthProperties;

	@Override
	public OAuthProvider getProvider() {
		return OAuthProvider.GOOGLE;
	}

	@Override
	public OAuthUserInfo getUserInfo(String accessToken) {
		try {
			return googleUserInfo(accessToken);
		} catch (HttpStatusCodeException e) {
			log.warn("[google-userinfo] failed status={} headers={} body={}",
					e.getStatusCode().value(),
					e.getResponseHeaders(),
					sanitize(e.getResponseBodyAsString()),
					e
			);
			throw new NeurousException(ErrorMessage.OAUTH2_GOOGLE_API_ERROR, e);
		} catch (RestClientException e) {
			log.warn("[google-userinfo] rest client error msg={}", e.getMessage(), e);
			throw new NeurousException(ErrorMessage.OAUTH2_GOOGLE_API_ERROR, e);
		} catch (Exception e) {
			log.error("[google-userinfo] unexpected error", e);
			throw new NeurousException(ErrorMessage.OAUTH2_GOOGLE_API_ERROR, e);
		}
	}

	private GoogleUserInfo googleUserInfo(String accessToken) {
		String url = oAuthProperties.getGoogle().getUserInfoUrl();
		HttpHeaders headers = createAuthHeaders(accessToken);
		HttpEntity<Void> request = new HttpEntity<>(headers);

		ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
			url,
			HttpMethod.GET,
			request,
			GoogleUserInfo.class
		);

		return response.getBody();
	}

	private HttpHeaders createAuthHeaders(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		return headers;
	}

	@Override
	public void unlink(String token) {
		String url = oAuthProperties.getGoogle().getRevokeUrl();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("token", token);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(
					url,
					HttpMethod.POST,
					request,
					String.class
			);

			log.info("[google-unlink] success status={} body={}",
					response.getStatusCode().value(),
					sanitize(response.getBody())
			);

		} catch (HttpStatusCodeException e) {
			log.warn("[google-unlink] failed status={} headers={} body={}",
					e.getStatusCode().value(),
					e.getResponseHeaders(),
					sanitize(e.getResponseBodyAsString()),
					e
			);
			throw new NeurousException(ErrorMessage.OAUTH2_GOOGLE_API_ERROR, e);

		} catch (RestClientException e) {
			log.warn("[google-unlink] rest client error msg={}", e.getMessage(), e);
			throw new NeurousException(ErrorMessage.OAUTH2_GOOGLE_API_ERROR, e);
		}
	}

	private String sanitize(String body) {
		if (body == null) return null;
		return body;
	}
}

