package com.example.server.global.exception;

import com.example.server.global.exception.model.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.server.global.exception.dto.ErrorResponse;
import com.example.server.global.exception.message.ErrorMessage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBadRequestException(final BadRequestException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.of(e.getErrorMessage()));
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ErrorResponse> handleUnauthorizedException(final UnauthorizedException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(ErrorResponse.of(e.getErrorMessage()));
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ErrorResponse> handleForbiddenException(final ForbiddenException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
			.body(ErrorResponse.of(e.getErrorMessage()));
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFoundException(final NotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ErrorResponse.of(e.getErrorMessage()));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflictException(final ConflictException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(ErrorResponse.of(e.getErrorMessage()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
		final HttpMessageNotReadableException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.of(ErrorMessage.NS_ENUM_VALUE_BAD_REQUEST));
	}

	//Enum 변환 실패 * PathVariable이나 QueryParam 시 발생하는 예외 처리
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.of(ErrorMessage.NS_ENUM_VALUE_BAD_REQUEST));
	}

	@ExceptionHandler(NeurousException.class)
	public ResponseEntity<ErrorResponse> handleNeurous(NeurousException e, HttpServletRequest req) {

		log.warn("[NeurousException] {} {} -> code={}, message={}",
				req.getMethod(), req.getRequestURI(),
				e.getErrorMessage().getCode(),
				e.getMessage()
		);

		return ResponseEntity
				.status(e.getErrorMessage().getStatus())
				.body(ErrorResponse.of(
						e.getErrorMessage().getStatus(),
						e.getErrorMessage().getCode(),
						e.getMessage()
				));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest req) {

		log.error("[UnhandledException] {} {}", req.getMethod(), req.getRequestURI(), e);

		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(ErrorMessage.INTERNAL_SERVER_ERROR));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest req) {

		log.warn("[NO-RESOURCE] {} {} -> {}", req.getMethod(), req.getRequestURI(), e.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ErrorResponse.of(404, "NOT_FOUND", "요청한 API를 찾을 수 없습니다."));
	}

}
