package com.propertybilling.integration.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.propertybilling.integration.AbstractIntegrationTest;

/*
 * Integration tests for property update across HTTP, persistence, and token validation.
 */
class PropertyUpdateIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyRepository propertyRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;

	@Autowired
	PropertyUpdateIntegrationTest(
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
	}

	@Test
	void putUpdatesPropertyNameAndAddress() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/properties/00000000-0000-0000-0000-000000000101")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Blue Residence",
								  "address": "Jakarta"
								}
								"""))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		assertThat(propertyRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000101")))
				.get()
				.satisfies(property -> {
					assertThat(property.getName()).isEqualTo("Blue Residence");
					assertThat(property.getAddress()).isEqualTo("Jakarta");
				});
	}

	@Test
	void putReturnsNotFoundWhenPropertyDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/properties/00000000-0000-0000-0000-000000000999")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Blue Residence",
								  "address": "Jakarta"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));
	}

	@Test
	void putRejectsMissingAuthorizationHeader() throws Exception {
		mockMvc.perform(put("/api/v1/properties/00000000-0000-0000-0000-000000000101")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Blue Residence",
								  "address": "Jakarta"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void putRejectsBlankName() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/properties/00000000-0000-0000-0000-000000000101")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "  ",
								  "address": "Jakarta"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));
	}

	private Property buildProperty(
			String id,
			String name,
			String address,
			boolean active
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-21T09:00:00Z");
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
