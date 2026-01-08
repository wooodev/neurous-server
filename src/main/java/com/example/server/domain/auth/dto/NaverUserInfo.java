package com.example.server.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfo implements OAuthUserInfo {
	@JsonProperty("resultcode")
	private String resultCode;
	private String message;
	private Response response; // 실제 정보는 여기 담김

	@Getter
	@NoArgsConstructor
	public static class Response {
		private String id;
		private String email;
		private String name;
	}

	@Override
	public String getProviderId() {
		return response.id;
	}

	@Override
	public String getEmail() {
		return response.email;
	}

	@Override
	public String getName() {
		String name = (response != null) ? response.name : null;
		if (hasValue(name)) return name;
		return generateFallbackName();
	}
}
