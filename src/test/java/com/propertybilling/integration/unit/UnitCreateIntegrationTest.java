package com.propertybilling.integration.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for creating units across HTTP, persistence, validation, and token validation.
 */
class UnitCreateIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyRepository propertyRepository;
	private final UnitRepository unitRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private Property property;
	private Property otherProperty;
	private User user;

	@Autowired
	UnitCreateIntegrationTest(
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
		otherProperty = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000102",
				"Blue Terrace",
				null,
				true
		));
	}

	@Test
	void createPersistsActiveUnit() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": "A-101",
								  "monthly_fee": "750000.00",
								  "due_day": 10
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		List<Unit> units = unitRepository.findAll();
		assertThat(units).hasSize(1);
		assertThat(units.getFirst().getProperty().getId()).isEqualTo(property.getId());
		assertThat(units.getFirst().getUnitNumber()).isEqualTo("A-101");
		assertThat(units.getFirst().getMonthlyFee()).isEqualTo("750000.00");
		assertThat(units.getFirst().getDueDay()).isEqualTo(10);
		assertThat(units.getFirst().isActive()).isTrue();
	}

	@Test
	void createRejectsDuplicateUnitNumberInSameProperty() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);
		unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000201",
				property,
				"A-101",
				"750000.00",
				10,
				true
		));

		mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": "A-101",
								  "monthly_fee": "800000.00",
								  "due_day": 15
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(content().string(""));
	}

	@Test
	void createAllowsSameUnitNumberInDifferentProperty() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);
		unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000201",
				property,
				"A-101",
				"750000.00",
				10,
				true
		));

		mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000102/units")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": "A-101",
								  "monthly_fee": "800000.00",
								  "due_day": 15
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		List<Unit> units = unitRepository.findAll();
		assertThat(units).hasSize(2);
		assertThat(units)
				.anySatisfy(unit -> {
					assertThat(unit.getProperty().getId()).isEqualTo(otherProperty.getId());
					assertThat(unit.getUnitNumber()).isEqualTo("A-101");
					assertThat(unit.getMonthlyFee()).isEqualTo("800000.00");
				});
	}

	@Test
	void createRejectsInvalidMonthlyFee() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": "A-101",
								  "monthly_fee": "0",
								  "due_day": 10
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));
	}

	@Test
	void createRejectsInvalidDueDay() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": "A-101",
								  "monthly_fee": "750000.00",
								  "due_day": 29
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));
	}

	@Test
	void createReturnsNotFoundWhenPropertyDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000999/units")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": "A-101",
								  "monthly_fee": "750000.00",
								  "due_day": 10
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));
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
