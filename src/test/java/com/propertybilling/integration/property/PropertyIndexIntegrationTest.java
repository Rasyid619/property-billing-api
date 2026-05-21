package com.propertybilling.integration.property;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.Property;
import com.propertybilling.entity.User;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.propertybilling.integration.AbstractIntegrationTest;

/*
 * Integration tests for property index behavior across HTTP, persistence, and token validation.
 */
class PropertyIndexIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyRepository propertyRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;

	@Autowired
	PropertyIndexIntegrationTest(
			MockMvc mockMvc,
			PropertyRepository propertyRepository,
			UserRepository userRepository,
			JwtTokenService jwtTokenService
	) {
		this.mockMvc = mockMvc;
		this.propertyRepository = propertyRepository;
		this.userRepository = userRepository;
		this.jwtTokenService = jwtTokenService;
	}

	@BeforeEach
	void setUp() {
		propertyRepository.deleteAll();
		userRepository.deleteAll();
		user = userRepository.save(new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"Admin User",
				"admin@example.com",
				"password-hash",
				"admin"
		));
		propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000101",
				"Green Residence",
				"Bekasi",
				true
		));
		propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000102",
				"Blue Terrace",
				null,
				false
		));
	}

	@Test
	void indexReturnsProperties() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/properties")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(2))
				.andExpect(jsonPath("$.properties[0].name").value("Blue Terrace"))
				.andExpect(jsonPath("$.properties[0].active").value(false))
				.andExpect(jsonPath("$.properties[1].name").value("Green Residence"))
				.andExpect(jsonPath("$.properties[1].address").value("Bekasi"))
				.andExpect(jsonPath("$.properties[1].active").value(true))
				.andExpect(jsonPath("$.properties[1].created_at").doesNotExist())
				.andExpect(jsonPath("$.properties[1].updated_at").doesNotExist());
	}

	@Test
	void indexFiltersPropertiesBySearch() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/properties")
						.header("Authorization", "Bearer " + accessToken)
						.param("search", "green"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.properties[0].name").value("Green Residence"));
	}

	@Test
	void indexFiltersPropertiesByStatus() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/properties")
						.header("Authorization", "Bearer " + accessToken)
						.param("status", "inactive"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.properties[0].name").value("Blue Terrace"))
				.andExpect(jsonPath("$.properties[0].active").value(false));
	}

	@Test
	void indexReturnsNoPropertiesForUnsupportedStatus() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/properties")
						.header("Authorization", "Bearer " + accessToken)
						.param("status", "archived"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(0))
				.andExpect(jsonPath("$.properties").isEmpty());
	}

	@Test
	void indexRejectsMissingAuthorizationHeader() throws Exception {
		mockMvc.perform(get("/api/v1/properties"))
				.andExpect(status().isUnauthorized());
	}

	private Property buildProperty(
			String id,
			String name,
			String address,
			boolean active
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-20T09:00:00Z");
		return new Property(
				UUID.fromString(id),
				name,
				address,
				active,
				timestamp,
				timestamp
		);
	}
}
