package com.propertybilling.integration.expense;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.Property;
import com.propertybilling.entity.PropertyExpense;
import com.propertybilling.entity.User;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.PropertyExpenseRepository;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for property expense index behavior across HTTP, persistence, and token validation.
 */
class ExpenseIndexIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyExpenseRepository propertyExpenseRepository;
	private final PropertyRepository propertyRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;
	private Property property;

	@Autowired
	ExpenseIndexIntegrationTest(
			MockMvc mockMvc,
			PropertyExpenseRepository propertyExpenseRepository,
			PropertyRepository propertyRepository,
			UserRepository userRepository,
			JwtTokenService jwtTokenService
	) {
		this.mockMvc = mockMvc;
		this.propertyExpenseRepository = propertyExpenseRepository;
		this.propertyRepository = propertyRepository;
		this.userRepository = userRepository;
		this.jwtTokenService = jwtTokenService;
	}

	@BeforeEach
	void setUp() {
		user = userRepository.save(new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"Admin User",
				"admin@example.com",
				"password-hash",
				"admin"
		));
		property = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000101",
				"Green Residence"
		));
		Property otherProperty = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000102",
				"Blue Terrace"
		));
		propertyExpenseRepository.save(buildExpense(
				"00000000-0000-0000-0000-000000000201",
				property,
				"2026-05-12",
				"cleaning",
				"750000.00",
				"Monthly cleaning fee"
		));
		propertyExpenseRepository.save(buildExpense(
				"00000000-0000-0000-0000-000000000202",
				property,
				"2026-05-18",
				"repair",
				"1250000.00",
				null
		));
		propertyExpenseRepository.save(buildExpense(
				"00000000-0000-0000-0000-000000000203",
				property,
				"2026-04-30",
				"security",
				"900000.00",
				"April security"
		));
		propertyExpenseRepository.save(buildExpense(
				"00000000-0000-0000-0000-000000000204",
				otherProperty,
				"2026-05-20",
				"water",
				"300000.00",
				"Other property"
		));
	}

	@Test
	void indexReturnsExpensesForPropertyOrderedByNewestDate() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/expenses")
						.header("Authorization", "Bearer " + accessToken)
						.param("property_id", property.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(3))
				.andExpect(jsonPath("$.expenses[0].id").value("00000000-0000-0000-0000-000000000202"))
				.andExpect(jsonPath("$.expenses[0].property_id").value(property.getId().toString()))
				.andExpect(jsonPath("$.expenses[0].expense_date").value("2026-05-18"))
				.andExpect(jsonPath("$.expenses[0].category").value("repair"))
				.andExpect(jsonPath("$.expenses[0].amount").value(1250000.00))
				.andExpect(jsonPath("$.expenses[0].description").doesNotExist())
				.andExpect(jsonPath("$.expenses[0].created_at").doesNotExist())
				.andExpect(jsonPath("$.expenses[0].updated_at").doesNotExist())
				.andExpect(jsonPath("$.expenses[1].id").value("00000000-0000-0000-0000-000000000201"))
				.andExpect(jsonPath("$.expenses[2].id").value("00000000-0000-0000-0000-000000000203"));
	}

	@Test
	void indexFiltersExpensesByMonth() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/expenses")
						.header("Authorization", "Bearer " + accessToken)
						.param("property_id", property.getId().toString())
						.param("month", "2026-05"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(2))
				.andExpect(jsonPath("$.expenses[0].id").value("00000000-0000-0000-0000-000000000202"))
				.andExpect(jsonPath("$.expenses[1].id").value("00000000-0000-0000-0000-000000000201"));
	}

	@Test
	void indexPaginatesExpenses() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/expenses")
						.header("Authorization", "Bearer " + accessToken)
						.param("property_id", property.getId().toString())
						.param("offset", "1")
						.param("limit", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.expenses[0].id").value("00000000-0000-0000-0000-000000000201"));
	}

	private Property buildProperty(String id, String name) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-20T09:00:00Z");
		return new Property(
				UUID.fromString(id),
				name,
				null,
				true,
				timestamp,
				timestamp
		);
	}

	private PropertyExpense buildExpense(
			String id,
			Property property,
			String expenseDate,
			String category,
			String amount,
			String description
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-20T09:00:00Z");
		return new PropertyExpense(
				UUID.fromString(id),
				property,
				LocalDate.parse(expenseDate),
				category,
				amount,
				description,
				null,
				timestamp,
				timestamp
		);
	}
}
