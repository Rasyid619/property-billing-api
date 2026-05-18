package com.propertybilling.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.config.SecurityConfig;
import com.propertybilling.dto.auth.AuthTokenResponse;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.exception.InvalidCredentialsException;
import com.propertybilling.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class AuthControllerTest {

	private final MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@Autowired
	AuthControllerTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void loginReturnsTokens() throws Exception {
		when(authService.login(any())).thenReturn(new AuthTokenResponse("access-token", "refresh-token"));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@example.com",
								  "password": "password123"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").value("access-token"))
				.andExpect(jsonPath("$.refresh_token").value("refresh-token"));
	}

	@Test
	void loginRejectsInvalidRequest() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "not-an-email",
								  "password": "short"
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void loginRejectsInvalidCredentials() throws Exception {
		when(authService.login(any())).thenThrow(new InvalidCredentialsException());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@example.com",
								  "password": "password123"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}
}
