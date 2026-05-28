package com.propertybilling.exception;

import jakarta.validation.ConstraintViolationException;
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

	@ExceptionHandler(PropertyNotFoundException.class)
	ResponseEntity<Void> handlePropertyNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@ExceptionHandler(UnitNotFoundException.class)
	ResponseEntity<Void> handleUnitNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@ExceptionHandler(TenantNotFoundException.class)
	ResponseEntity<Void> handleTenantNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@ExceptionHandler(TenantAssignmentNotFoundException.class)
	ResponseEntity<Void> handleTenantAssignmentNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@ExceptionHandler(TenantAssignmentConflictException.class)
	ResponseEntity<Void> handleTenantAssignmentConflict() {
		return ResponseEntity.status(HttpStatus.CONFLICT).build();
	}

	@ExceptionHandler(TenantAssignmentMoveOutDateException.class)
	ResponseEntity<Void> handleTenantAssignmentMoveOutDate() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}

	@ExceptionHandler(UnitNumberConflictException.class)
	ResponseEntity<Void> handleUnitNumberConflict() {
		return ResponseEntity.status(HttpStatus.CONFLICT).build();
	}

	@ExceptionHandler(TenantContactConflictException.class)
	ResponseEntity<Void> handleTenantContactConflict() {
		return ResponseEntity.status(HttpStatus.CONFLICT).build();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<Void> handleMethodArgumentNotValid() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<Void> handleHttpMessageNotReadable() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<Void> handleConstraintViolation() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}

}
