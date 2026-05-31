package com.propertybilling.integration.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
 * Integration tests for property expense replacement across HTTP, persistence, and token validation.
 */
class ExpenseUpdateIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyExpenseRepository propertyExpenseRepository;
	private final PropertyRepository propertyRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;
	private Property property;
	private Property otherProperty;

	@Autowired
	ExpenseUpdateIntegrationTest(
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
		otherProperty = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000102",
				"Blue Terrace"
		));
		propertyExpenseRepository.save(buildExpense());
	}

	@Test
	void updateReplacesExpense() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/expenses/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000102",
								  "expense_date": "2026-05-15",
								  "category": "repair",
								  "amount": 1250000.00,
								  "description": "Gate repair"
								}
								"""))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		PropertyExpense expense = propertyExpenseRepository.findById(
				UUID.fromString("00000000-0000-0000-0000-000000000201")
		).orElseThrow();
		assertThat(expense.getProperty().getId()).isEqualTo(otherProperty.getId());
		assertThat(expense.getExpenseDate()).isEqualTo(LocalDate.parse("2026-05-15"));
		assertThat(expense.getCategory()).isEqualTo("repair");
		assertThat(expense.getAmount()).isEqualTo("1250000.00");
		assertThat(expense.getDescription()).isEqualTo("Gate repair");
		assertThat(expense.getReceiptUrl()).isNull();
	}

	@Test
	void updateRejectsZeroAmount() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/expenses/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000102",
								  "expense_date": "2026-05-15",
								  "category": "repair",
								  "amount": 0
								}
								"""))
				.andExpect(status().isBadRequest());

		assertExpenseWasNotChanged();
	}

	@Test
	void updateRejectsNegativeAmount() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/expenses/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000102",
								  "expense_date": "2026-05-15",
								  "category": "repair",
								  "amount": -1
								}
								"""))
				.andExpect(status().isBadRequest());

		assertExpenseWasNotChanged();
	}

	@Test
	void updateRejectsMissingCategory() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/expenses/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000102",
								  "expense_date": "2026-05-15",
								  "amount": 1250000.00
								}
								"""))
				.andExpect(status().isBadRequest());

		assertExpenseWasNotChanged();
	}

	@Test
	void updateHandlesExpenseNotFound() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/expenses/00000000-0000-0000-0000-000000000999")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000102",
								  "expense_date": "2026-05-15",
								  "category": "repair",
								  "amount": 1250000.00
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));

		assertExpenseWasNotChanged();
	}

	@Test
	void updateHandlesPropertyNotFound() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(put("/api/v1/expenses/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000999",
								  "expense_date": "2026-05-15",
								  "category": "repair",
								  "amount": 1250000.00
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));

		assertExpenseWasNotChanged();
	}

	private void assertExpenseWasNotChanged() {
		PropertyExpense expense = propertyExpenseRepository.findById(
				UUID.fromString("00000000-0000-0000-0000-000000000201")
		).orElseThrow();
		assertThat(expense.getProperty().getId()).isEqualTo(property.getId());
		assertThat(expense.getExpenseDate()).isEqualTo(LocalDate.parse("2026-05-12"));
		assertThat(expense.getCategory()).isEqualTo("cleaning");
		assertThat(expense.getAmount()).isEqualTo("750000.00");
		assertThat(expense.getDescription()).isEqualTo("Monthly cleaning fee");
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

	private PropertyExpense buildExpense() {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-20T09:00:00Z");
		return new PropertyExpense(
				UUID.fromString("00000000-0000-0000-0000-000000000201"),
				property,
				LocalDate.parse("2026-05-12"),
				"cleaning",
				"750000.00",
				"Monthly cleaning fee",
				null,
				timestamp,
				timestamp
		);
	}
}
