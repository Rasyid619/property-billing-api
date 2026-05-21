package com.propertybilling.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/*
 * Central translation layer from domain exceptions to HTTP responses.
 */
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<Void> handleInvalidCredentials() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	ResponseEntity<Void> handleInvalidRefreshToken() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@ExceptionHandler(InvalidAccessTokenException.class)
	ResponseEntity<Void> handleInvalidAccessToken() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	ResponseEntity<Void> handleMissingRequestHeader() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<Void> handleMethodArgumentNotValid() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<Void> handleHttpMessageNotReadable() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}

}
