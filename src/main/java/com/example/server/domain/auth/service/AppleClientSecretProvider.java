package com.example.server.domain.auth.service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.example.server.global.security.oauth.OAuthProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppleClientSecretProvider {

    private final OAuthProperties oAuthProperties;

    public String createClientSecret() {
        OAuthProperties.Apple apple = oAuthProperties.getApple();

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(300); // 5분

        PrivateKey privateKey = loadPrivateKey(apple.getPrivateKey());

        return Jwts.builder()
                .setHeaderParam("kid", apple.getKeyId())
                .setIssuer(apple.getTeamId())
                .setAudience("https://appleid.apple.com")
                .setSubject(apple.getClientId())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    private PrivateKey loadPrivateKey(String p8) {
        try {
            String key = p8
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Apple private key 로드 실패", e);
        }
    }
}