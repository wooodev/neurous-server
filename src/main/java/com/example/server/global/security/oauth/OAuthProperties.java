package com.example.server.global.security.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

	private Kakao kakao = new Kakao();
	private Google google = new Google();
	private Naver naver = new Naver();
	private Apple apple = new Apple();

	@Getter
	@Setter
	public static class Kakao {
		private String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
		private String unlinkUrl = "https://kapi.kakao.com/v1/user/unlink";
	}

	@Getter
	@Setter
	public static class Google {
		private String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
		private String revokeUrl = "https://oauth2.googleapis.com/revoke";
	}

	@Getter
	@Setter
	public static class Naver {
		private String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
		private String tokenUrl = "https://nid.naver.com/oauth2.0/token";
		private String clientId;
		private String clientSecret;
	}

	@Getter
	@Setter
	public static class Apple {
		private String tokenUrl = "https://appleid.apple.com/auth/token";
		private String revokeUrl = "https://appleid.apple.com/auth/revoke";

		// Apple Developer 설정 값들
		private String clientId;   // Services ID
		private String teamId;     // Team ID
		private String keyId;      // Key ID
		private String privateKey; // p8 내용을 문자열로(개행 포함 가능)
	}
}
