package com.propertybilling.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.User;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:auth_refresh_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.flyway.enabled=false",
		"app.jwt.secret=integration-test-secret"
})
@AutoConfigureMockMvc
/*
 * Integration tests for refresh-token behavior across HTTP, persistence, and token generation.
 */
class AuthRefreshIntegrationTest {

	private final MockMvc mockMvc;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;

	@Autowired
	AuthRefreshIntegrationTest(
			MockMvc mockMvc,
			UserRepository userRepository,
			JwtTokenService jwtTokenService
	) {
		this.mockMvc = mockMvc;
		this.userRepository = userRepository;
		this.jwtTokenService = jwtTokenService;
	}

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		user = userRepository.save(new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"admin@example.com",
				"password-hash",
				"admin"
		));
	}

	@Test
	void refreshReturnsNewAccessTokenForValidRefreshToken() throws Exception {
		String refreshToken = jwtTokenService.createRefreshToken(user);

		mockMvc.perform(post("/api/v1/auth/refresh")
						.header("Authorization", "Bearer " + refreshToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").isString());
	}

	@Test
	void refreshRejectsAccessToken() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/auth/refresh")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isUnauthorized());
	}
}
