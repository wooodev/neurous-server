package com.example.server.global.redis;

import java.time.Duration;

import lombok.Getter;

@Getter
public enum RedisKey {

	//컨텐츠 조회수
	CONTENT_HITS("contents:hits", Duration.ofDays(1)),

	//metadata 캐싱 설정
	CHARACTER_LIST("metadata:character:all", Duration.ofDays(1)),
	CHARACTER_ITEM("metadata:character:", Duration.ofDays(1)),
	REWARD_DATA_LIST("metadata:reward:data:all", Duration.ofDays(1)),
	REWARD_INFO_LIST("metadata:reward:info:all", Duration.ofDays(1)),

	//미션 개수
	DAILY_MISSION("mission:daily:", null);

	private final String prefix;
	private final Duration ttl;

	RedisKey(String prefix, Duration ttl) {
		this.prefix = prefix;
		this.ttl = ttl;
	}

	public String getFullKey(Object suffix) {
		return this.prefix + suffix;
	}

}
