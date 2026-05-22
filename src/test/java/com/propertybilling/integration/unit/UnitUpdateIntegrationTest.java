package com.propertybilling.integration.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for unit update across HTTP, persistence, validation, and token validation.
 */
class UnitUpdateIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyRepository propertyRepository;
	private final UnitRepository unitRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private Property property;
	private User user;

	@Autowired
	UnitUpdateIntegrationTest(
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
				true
		));
	}

	@Test
	void putUpdatesUnitFields() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/units/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": "A-103",
								  "monthly_fee": "900000.00",
								  "due_day": 20
								}
								"""))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		assertThat(unitRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000201")))
				.get()
				.satisfies(unit -> {
					assertThat(unit.getUnitNumber()).isEqualTo("A-103");
					assertThat(unit.getMonthlyFee()).isEqualTo("900000.00");
					assertThat(unit.getDueDay()).isEqualTo(20);
					assertThat(unit.isActive()).isTrue();
				});
	}

	@Test
	void putReturnsConflictWhenAnotherUnitAlreadyUsesNumber() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/units/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": "A-102",
								  "monthly_fee": "900000.00",
								  "due_day": 20
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(content().string(""));
	}

	@Test
	void putReturnsNotFoundWhenUnitDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/units/00000000-0000-0000-0000-000000000999")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": "A-103",
								  "monthly_fee": "900000.00",
								  "due_day": 20
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));
	}

	@Test
	void putRejectsBlankUnitNumber() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/units/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": " ",
								  "monthly_fee": "900000.00",
								  "due_day": 20
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));
	}

	@Test
	void putRejectsInvalidMonthlyFee() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/units/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": "A-103",
								  "monthly_fee": "0",
								  "due_day": 20
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));
	}

	@Test
	void putRejectsInvalidDueDay() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/units/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "unit_number": "A-103",
								  "monthly_fee": "900000.00",
								  "due_day": 29
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
