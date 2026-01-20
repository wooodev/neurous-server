package com.example.server.global.exception.model;

import com.example.server.global.exception.message.ErrorMessage;

import lombok.Getter;

@Getter
public class NeurousException extends RuntimeException {

	private final ErrorMessage errorMessage;

	public NeurousException(ErrorMessage errorMessage) {
		super(errorMessage.getMessage());
		this.errorMessage = errorMessage;
	}
	public NeurousException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage.getMessage(), cause);
		this.errorMessage = errorMessage;
	}

	public ErrorMessage getErrorMessage() {
		return errorMessage;
	}

}
