package com.example.server.global.security.config;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.example.server.global.exception.dto.ErrorResponse;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> {

					log.warn("[AUTH1008] 인증 필요: method={} uri={} ip={} ua={} authHeaderPresent={} msg={}",
							request.getMethod(),
							request.getRequestURI(),
							request.getRemoteAddr(),
							request.getHeader("User-Agent"),
							request.getHeader("Authorization") != null,
							authException != null ? authException.getMessage() : null
					);

					ErrorResponse errorResponse = ErrorResponse.of(ErrorMessage.NEED_CERTIFICATION);
					response.setContentType("application/json;charset=UTF-8");
					response.setStatus(401);
					String json = new com.fasterxml.jackson.databind.ObjectMapper()
						.writeValueAsString(errorResponse);
					response.getWriter().write(json);
				})
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/auth/login/**",
					"/api/auth/refresh",
					"/swagger-ui/**",
					"/v3/api-docs/**",
					"/swagger-ui.html",
					"/swagger-resources/**",
					"/api/test/**"
				).permitAll()
				.anyRequest().authenticated() // 나머지 요청은 인증 필요
			)
			// JWT 필터 추가
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		return request -> {
			CorsConfiguration configuration = new CorsConfiguration();
			configuration.setAllowedOrigins(List.of("http://localhost:3000"));
			configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
			configuration.setAllowedHeaders(List.of("*"));
			configuration.setAllowCredentials(true);
			configuration.setMaxAge(3600L);
			configuration.setExposedHeaders(List.of("Authorization"));
			return configuration;
		};
	}

}
