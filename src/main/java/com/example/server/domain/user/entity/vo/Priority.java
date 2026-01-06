package com.example.server.domain.user.entity.vo;

import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.BadRequestException;

// 우선순위
public enum Priority {
	ONE_ST_PLACE(1),
	TWO_ND_PLACE(2),
	THIRD_RD_PLACE(3);

	private final int order;

	Priority(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public static Priority fromIndex(int index) {
		if (index < 0 || index >= values().length) {
			throw new BadRequestException(ErrorMessage.USER_INVALID_INTEREST_COUNT);
		}
		return values()[index];
	}
}
