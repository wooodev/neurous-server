package com.example.server.domain.auth.service;

import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NeurousException;
import com.example.server.global.security.oauth.OAuthProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleOAuthTokenService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OAuthProperties oAuthProperties;
    private final AppleClientSecretProvider appleClientSecretProvider;

    /**
     * (최초 1회만 내려오는 경우가 많음) authorizationCode로 refresh_token 확보
     */
    @Transactional(readOnly = true)
    public String exchangeAuthorizationCodeForRefreshToken(String authorizationCode) {
        try {
            OAuthProperties.Apple apple = oAuthProperties.getApple();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", authorizationCode);
            body.add("client_id", apple.getClientId());
            body.add("client_secret", appleClientSecretProvider.createClientSecret());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    apple.getTokenUrl(),
                    request,
                    String.class
            );

            JsonNode node = objectMapper.readTree(response.getBody());
            return node.path("refresh_token").asText(null);
        } catch (RestClientException e) {
            throw new NeurousException(ErrorMessage.OAUTH2_APPLE_API_ERROR);
        } catch (Exception e) {
            throw new NeurousException(ErrorMessage.OAUTH2_APPLE_API_ERROR);
        }
    }

    public void revokeByRefreshToken(String refreshToken) {
        try {
            OAuthProperties.Apple apple = oAuthProperties.getApple();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", apple.getClientId());
            body.add("client_secret", appleClientSecretProvider.createClientSecret());
            body.add("token", refreshToken);
            body.add("token_type_hint", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    apple.getRevokeUrl(),
                    request,
                    String.class
            );
        } catch (RestClientException e) {
            throw new NeurousException(ErrorMessage.OAUTH2_APPLE_API_ERROR);
        }
    }
}