package com.example.server.domain.user.dto.response;

import java.util.Comparator;
import java.util.List;

import com.example.server.domain.user.entity.vo.UserInterest;

public record UserInterestsResponse(
	List<UserInterestResponse> interests
) {
	public static UserInterestsResponse from(List<UserInterest> interests) {
		return new UserInterestsResponse(
			interests.stream()
				.sorted(Comparator.comparing(i -> i.getPriority().getOrder()))
				.map(UserInterestResponse::from)
				.toList()
		);
	}
}
