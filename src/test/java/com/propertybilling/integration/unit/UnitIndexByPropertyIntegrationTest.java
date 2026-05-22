package com.propertybilling.integration.unit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.Property;
import com.propertybilling.entity.Unit;
import com.propertybilling.entity.User;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.UnitRepository;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for listing units by property across HTTP, persistence, and token validation.
 */
class UnitIndexByPropertyIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyRepository propertyRepository;
	private final UnitRepository unitRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private Property property;
	private User user;

	@Autowired
	UnitIndexByPropertyIntegrationTest(
			MockMvc mockMvc,
			PropertyRepository propertyRepository,
			UnitRepository unitRepository,
			UserRepository userRepository,
			JwtTokenService jwtTokenService
	) {
		this.mockMvc = mockMvc;
		this.propertyRepository = propertyRepository;
		this.unitRepository = unitRepository;
		this.userRepository = userRepository;
		this.jwtTokenService = jwtTokenService;
	}

	@BeforeEach
	void setUp() {
		unitRepository.deleteAll();
		propertyRepository.deleteAll();
		userRepository.deleteAll();
		user = userRepository.save(new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"Admin User",
				"admin@example.com",
				"password-hash",
				"admin"
		));
		property = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000101",
				"Green Residence",
				"Bekasi",
				true
		));
		Property otherProperty = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000102",
				"Blue Terrace",
				null,
				true
		));
		unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000201",
				property,
				"A-101",
				"750000.00",
				10,
				true
		));
		unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000202",
				property,
				"A-102",
				"800000.00",
				15,
				false
		));
		unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000203",
				otherProperty,
				"B-101",
				"650000.00",
				8,
				true
		));
	}

	@Test
	void indexReturnsUnitsByProperty() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(2))
				.andExpect(jsonPath("$.units[0].id").value("00000000-0000-0000-0000-000000000201"))
				.andExpect(jsonPath("$.units[0].property_id").value("00000000-0000-0000-0000-000000000101"))
				.andExpect(jsonPath("$.units[0].unit_number").value("A-101"))
				.andExpect(jsonPath("$.units[0].monthly_fee").value(750000.00))
				.andExpect(jsonPath("$.units[0].due_day").value(10))
				.andExpect(jsonPath("$.units[0].active").value(true))
				.andExpect(jsonPath("$.units[0].created_at").doesNotExist())
				.andExpect(jsonPath("$.units[0].updated_at").doesNotExist())
				.andExpect(jsonPath("$.units[1].unit_number").value("A-102"))
				.andExpect(jsonPath("$.units[1].active").value(false));
	}

	@Test
	void indexFiltersUnitsByStatus() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
						.header("Authorization", "Bearer " + accessToken)
						.param("status", "inactive"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.units[0].unit_number").value("A-102"))
				.andExpect(jsonPath("$.units[0].active").value(false));
	}

	@Test
	void indexTreatsNullStatusAsNoFilter() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
						.header("Authorization", "Bearer " + accessToken)
						.param("status", "null"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(2));
	}

	@Test
	void indexReturnsNotFoundWhenPropertyDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000999/units")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void indexRejectsMissingAuthorizationHeader() throws Exception {
		mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000101/units"))
				.andExpect(status().isUnauthorized());
	}

	private Property buildProperty(
			String id,
			String name,
			String address,
			boolean active
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Property(
				UUID.fromString(id),
				name,
				address,
				active,
				timestamp,
				timestamp
		);
	}

	private Unit buildUnit(
			String id,
			Property property,
			String unitNumber,
			String monthlyFee,
			int dueDay,
			boolean active
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Unit(
				UUID.fromString(id),
				property,
				unitNumber,
				monthlyFee,
				dueDay,
				active,
				timestamp,
				timestamp
		);
	}
}
