package com.example.server.global.security.jwt;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.server.domain.user.entity.User;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.global.security.principal.UserPrincipal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		try {
			authenticateRequest(request);
		} catch (Exception e) {
			log.error("JWT 인증 실패", e);
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	private void authenticateRequest(HttpServletRequest request) {
		String token = extractTokenFromRequest(request);

		if (token == null || !jwtTokenProvider.validateToken(token)) {
			return;
		}

		Long userId = jwtTokenProvider.getUserIdFromToken(token);
		String email = jwtTokenProvider.getEmailFromToken(token);

		UserPrincipal userPrincipal = new UserPrincipal(userId, email);

		setAuthentication(userPrincipal);
	}

	private String extractTokenFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

		if (hasBearerToken(bearerToken)) {
			return extractToken(bearerToken);
		}

		return null;
	}

	private boolean hasBearerToken(String bearerToken) {
		return StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX);
	}

	private String extractToken(String bearerToken) {
		return bearerToken.substring(BEARER_PREFIX.length());
	}

	private boolean isTokenAbsent(String token) {
		return isTokenNull(token) || isTokenInvalid(token);
	}

	private boolean isTokenNull(String token) {
		return token == null;
	}

	private boolean isTokenInvalid(String token) {
		return !jwtTokenProvider.validateToken(token);
	}

	private User getUserFromToken(String token) {
		Long userId = jwtTokenProvider.getUserIdFromToken(token);
		return userRepository.findById(userId).orElse(null);
	}

	private boolean isUserAbsent(User user) {
		return user == null;
	}

	private void setAuthentication(UserPrincipal principal) {
		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
