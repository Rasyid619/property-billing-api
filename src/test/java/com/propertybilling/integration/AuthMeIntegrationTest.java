package com.propertybilling.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.User;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for current-user behavior across HTTP, persistence, and token validation.
 */
class AuthMeIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;

	@Autowired
	AuthMeIntegrationTest(
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
				"Admin User",
				"admin@example.com",
				"password-hash",
				"admin"
		));
	}

	@Test
	void meReturnsAuthenticatedUser() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/auth/me")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000001"))
				.andExpect(jsonPath("$.name").value("Admin User"))
				.andExpect(jsonPath("$.email").value("admin@example.com"))
				.andExpect(jsonPath("$.role").value("admin"));
	}

}
