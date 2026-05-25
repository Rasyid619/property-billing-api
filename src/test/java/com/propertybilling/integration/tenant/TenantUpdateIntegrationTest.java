package com.propertybilling.integration.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for tenant update behavior across HTTP, persistence, and token validation.
 */
class TenantUpdateIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final TenantRepository tenantRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;

	@Autowired
	TenantUpdateIntegrationTest(
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
	void updateReplacesTenantFields() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(patch("/api/v1/tenants/00000000-0000-0000-0000-000000000301")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Andi",
								  "phone": "08111111111",
								  "email": "andi@example.com"
								}
								"""))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		Tenant tenant = tenantRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000301"))
				.orElseThrow();
		assertThat(tenant.getName()).isEqualTo("Andi");
		assertThat(tenant.getPhone()).isEqualTo("08111111111");
		assertThat(tenant.getEmail()).isEqualTo("andi@example.com");
	}

	@Test
	void updateRejectsBlankName() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(patch("/api/v1/tenants/00000000-0000-0000-0000-000000000301")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": " ",
								  "phone": "08111111111",
								  "email": "andi@example.com"
								}
								"""))
				.andExpect(status().isBadRequest());

		Tenant tenant = tenantRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000301"))
				.orElseThrow();
		assertThat(tenant.getName()).isEqualTo("Budi");
		assertThat(tenant.getPhone()).isEqualTo("08123456789");
		assertThat(tenant.getEmail()).isEqualTo("budi@example.com");
	}

	@Test
	void updateReturnsNotFoundWhenTenantDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(patch("/api/v1/tenants/00000000-0000-0000-0000-000000000999")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Andi",
								  "phone": "08111111111",
								  "email": "andi@example.com"
								}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateRejectsDuplicatePhone() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);
		tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000302",
				"Sari",
				"08111111111",
				"sari@example.com"
		));

		mockMvc.perform(patch("/api/v1/tenants/00000000-0000-0000-0000-000000000301")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Andi",
								  "phone": "08111111111",
								  "email": "andi@example.com"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(content().string(""));
	}

	@Test
	void updateRejectsDuplicateEmail() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);
		tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000302",
				"Sari",
				"08111111111",
				"andi@example.com"
		));

		mockMvc.perform(patch("/api/v1/tenants/00000000-0000-0000-0000-000000000301")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Andi",
								  "phone": "08122222222",
								  "email": "andi@example.com"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(content().string(""));
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
