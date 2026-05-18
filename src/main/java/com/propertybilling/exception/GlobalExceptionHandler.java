package com.propertybilling.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/**
 * Central translation layer from domain exceptions to HTTP responses.
 */
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<Void> handleInvalidCredentials() {
		return ResponseEntity.status(401).build();
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	ResponseEntity<Void> handleInvalidRefreshToken() {
		return ResponseEntity.status(401).build();
	}
}
