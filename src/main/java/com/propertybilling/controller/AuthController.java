package com.propertybilling.controller;

import com.propertybilling.dto.auth.AuthTokenResponse;
import com.propertybilling.dto.auth.AccessTokenResponse;
import com.propertybilling.dto.auth.AuthMeResponse;
import com.propertybilling.dto.auth.LoginRequest;
import com.propertybilling.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
/*
 * HTTP endpoints for admin and staff authentication workflows.
 */
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@GetMapping("/me")
	ResponseEntity<AuthMeResponse> me(@RequestHeader("Authorization") String authorizationHeader) {
		return ResponseEntity.ok(authService.me(authorizationHeader));
	}

	@PostMapping("/refresh")
	ResponseEntity<AccessTokenResponse> refresh(@RequestHeader("Authorization") String authorizationHeader) {
		return ResponseEntity.ok(authService.refresh(authorizationHeader));
	}
}
