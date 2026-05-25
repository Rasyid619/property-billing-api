package com.propertybilling.integration.tenant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.User;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for tenant detail behavior across HTTP, persistence, and token validation.
 */
class TenantShowIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final TenantRepository tenantRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;

	@Autowired
	TenantShowIntegrationTest(
			MockMvc mockMvc,
			TenantRepository tenantRepository,
			UserRepository userRepository,
			JwtTokenService jwtTokenService
	) {
		this.mockMvc = mockMvc;
		this.tenantRepository = tenantRepository;
		this.userRepository = userRepository;
		this.jwtTokenService = jwtTokenService;
	}

	@BeforeEach
	void setUp() {
		tenantRepository.deleteAll();
		userRepository.deleteAll();
		user = userRepository.save(new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"Admin User",
				"admin@example.com",
				"password-hash",
				"admin"
		));
		tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000301",
				"Budi",
				"08123456789",
				"budi@example.com"
		));
	}

	@Test
	void showReturnsTenant() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/tenants/00000000-0000-0000-0000-000000000301")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000301"))
				.andExpect(jsonPath("$.name").value("Budi"))
				.andExpect(jsonPath("$.phone").value("08123456789"))
				.andExpect(jsonPath("$.email").value("budi@example.com"))
				.andExpect(jsonPath("$.created_at").doesNotExist())
				.andExpect(jsonPath("$.updated_at").doesNotExist());
	}

	@Test
	void showReturnsNotFoundWhenTenantDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/tenants/00000000-0000-0000-0000-000000000999")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNotFound());
	}

	private Tenant buildTenant(
			String id,
			String name,
			String phone,
			String email
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-25T09:00:00Z");
		return new Tenant(
				UUID.fromString(id),
				name,
				phone,
				email,
				timestamp,
				timestamp
		);
	}
}
