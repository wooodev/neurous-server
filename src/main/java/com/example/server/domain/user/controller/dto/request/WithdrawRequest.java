package com.example.server.domain.user.controller.dto.request;

public record WithdrawRequest(
        boolean unlinkSocial,
        String providerAccessToken,     // KAKAO/NAVER/GOOGLE용
        String appleAuthorizationCode   // APPLE refresh_token 확보용
) {
}