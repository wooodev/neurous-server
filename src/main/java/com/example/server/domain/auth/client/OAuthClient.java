package com.example.server.domain.auth.client;

import com.example.server.domain.auth.dto.OAuthUserInfo;
import com.example.server.domain.auth.enums.OAuthProvider;

public interface OAuthClient {
	OAuthUserInfo getUserInfo(String accessToken);

	OAuthProvider getProvider();

	void unlink(String token);
}
