package com.example.server.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.server.domain.auth.entity.TokenManager;
import com.example.server.domain.user.entity.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<TokenManager, Long>, RefreshTokenRepositoryCustom {

	Optional<TokenManager> findByUser(User user);

	Optional<TokenManager> findByTokenValue(String tokenValue);

	void deleteByUser(User user);
}