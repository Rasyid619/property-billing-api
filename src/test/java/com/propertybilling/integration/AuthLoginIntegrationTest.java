package com.propertybilling.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.User;
import com.propertybilling.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:auth_login_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.flyway.enabled=false",
		"app.jwt.secret=integration-test-secret"
})
@AutoConfigureMockMvc
/**
 * Integration tests for login behavior across HTTP, persistence, and token generation.
 */
class AuthLoginIntegrationTest {

	private final MockMvc mockMvc;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	AuthLoginIntegrationTest(
			MockMvc mockMvc,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder
	) {
		this.mockMvc = mockMvc;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		userRepository.save(new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"admin@example.com",
				passwordEncoder.encode("password123"),
				"admin"
		));
	}

	@Test
	void loginReturnsJwtTokensForValidCredentials() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@example.com",
								  "password": "password123"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").isString())
				.andExpect(jsonPath("$.refresh_token").isString());
	}

	@Test
	void loginRejectsInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@example.com",
								  "password": "wrong-password"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}
}
