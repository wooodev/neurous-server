package com.example.server.domain.user.dto.response;

import com.example.server.domain.user.entity.vo.Priority;
import com.example.server.domain.user.entity.vo.UserInterest;

public record UserInterestResponse(
	String key,
	String description,
	Priority priority
) {
	public static UserInterestResponse from(UserInterest interest) {
		return new UserInterestResponse(
			interest.getInterest().name(),
			interest.getInterest().getDescription(),
			interest.getPriority()
		);
	}
}
