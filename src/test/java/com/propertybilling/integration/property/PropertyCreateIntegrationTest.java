package com.propertybilling.integration.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.Property;
import com.propertybilling.entity.User;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:property_create_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.flyway.enabled=false",
		"app.jwt.secret=integration-test-secret"
})
@AutoConfigureMockMvc
/*
 * Integration tests for property creation across HTTP, persistence, and token validation.
 */
class PropertyCreateIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyRepository propertyRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;

	@Autowired
	PropertyCreateIntegrationTest(
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
	}

	@Test
	void createPersistsActiveProperty() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/properties")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Green Residence",
								  "address": "Bekasi"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		assertThat(propertyRepository.findAll())
				.singleElement()
				.satisfies(this::assertCreatedProperty);
	}

	@Test
	void createRejectsBlankName() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/properties")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": " ",
								  "address": "Bekasi"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));
	}

	@Test
	void createRejectsMissingAuthorizationHeader() throws Exception {
		mockMvc.perform(post("/api/v1/properties")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Green Residence",
								  "address": "Bekasi"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	private void assertCreatedProperty(Property property) {
		assertThat(property.getId()).isNotNull();
		assertThat(property.getName()).isEqualTo("Green Residence");
		assertThat(property.getAddress()).isEqualTo("Bekasi");
		assertThat(property.isActive()).isTrue();
		assertThat(property.getCreatedAt()).isNotNull();
		assertThat(property.getUpdatedAt()).isNotNull();
	}
}
