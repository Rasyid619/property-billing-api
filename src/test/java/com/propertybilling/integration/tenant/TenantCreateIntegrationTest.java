package com.propertybilling.integration.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.User;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for tenant creation across HTTP, persistence, and token validation.
 */
class TenantCreateIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final TenantRepository tenantRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;

	@Autowired
	TenantCreateIntegrationTest(
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
	}

	@Test
	void createPersistsTenant() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/tenants")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Budi",
								  "phone": "08123456789",
								  "email": "budi@example.com"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		var tenants = tenantRepository.findAll();
		assertThat(tenants).hasSize(1);
		assertThat(tenants.getFirst().getId()).isNotNull();
		assertThat(tenants.getFirst().getName()).isEqualTo("Budi");
		assertThat(tenants.getFirst().getPhone()).isEqualTo("08123456789");
		assertThat(tenants.getFirst().getEmail()).isEqualTo("budi@example.com");
		assertThat(tenants.getFirst().getCreatedAt()).isNotNull();
		assertThat(tenants.getFirst().getUpdatedAt()).isNotNull();
	}

	@Test
	void createRejectsBlankName() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/tenants")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": " ",
								  "phone": "08123456789",
								  "email": "budi@example.com"
								}
								"""))
				.andExpect(status().isBadRequest());

		assertThat(tenantRepository.findAll()).isEmpty();
	}

	@Test
	void createRejectsDuplicatePhone() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);
		tenantRepository.save(new Tenant(
				UUID.fromString("00000000-0000-0000-0000-000000000301"),
				"Andi",
				"08123456789",
				"andi@example.com"
		));

		mockMvc.perform(post("/api/v1/tenants")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Budi",
								  "phone": "08123456789",
								  "email": "budi@example.com"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(content().string(""));

		assertThat(tenantRepository.findAll()).hasSize(1);
	}

	@Test
	void createRejectsDuplicateEmail() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);
		tenantRepository.save(new Tenant(
				UUID.fromString("00000000-0000-0000-0000-000000000301"),
				"Andi",
				"08111111111",
				"budi@example.com"
		));

		mockMvc.perform(post("/api/v1/tenants")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Budi",
								  "phone": "08123456789",
								  "email": "budi@example.com"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(content().string(""));

		assertThat(tenantRepository.findAll()).hasSize(1);
	}
}
