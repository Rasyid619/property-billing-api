package com.propertybilling.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<Void> handleInvalidCredentials() {
		return ResponseEntity.status(401).build();
	}
}
