package com.propertybilling.integration.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for property expense creation across HTTP, persistence, and token validation.
 */
class ExpenseCreateIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyExpenseRepository propertyExpenseRepository;
	private final PropertyRepository propertyRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;
	private Property property;

	@Autowired
	ExpenseCreateIntegrationTest(
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
		property = propertyRepository.save(new Property(
				UUID.fromString("00000000-0000-0000-0000-000000000101"),
				"Green Residence",
				null,
				true,
				OffsetDateTime.parse("2026-05-20T09:00:00Z"),
				OffsetDateTime.parse("2026-05-20T09:00:00Z")
		));
	}

	@Test
	void createPersistsExpense() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/expenses")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "expense_date": "2026-05-12",
								  "category": "cleaning",
								  "amount": 750000.00,
								  "description": "Monthly cleaning fee"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		List<PropertyExpense> expenses = propertyExpenseRepository.findAll();
		assertThat(expenses).hasSize(1);
		PropertyExpense expense = expenses.getFirst();
		assertThat(expense.getProperty().getId()).isEqualTo(property.getId());
		assertThat(expense.getExpenseDate()).hasToString("2026-05-12");
		assertThat(expense.getCategory()).isEqualTo("cleaning");
		assertThat(expense.getAmount()).isEqualTo("750000.00");
		assertThat(expense.getDescription()).isEqualTo("Monthly cleaning fee");
		assertThat(expense.getReceiptUrl()).isNull();
	}

	@Test
	void createRejectsZeroAmount() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/expenses")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "expense_date": "2026-05-12",
								  "category": "cleaning",
								  "amount": 0
								}
								"""))
				.andExpect(status().isBadRequest());

		assertThat(propertyExpenseRepository.findAll()).isEmpty();
	}

	@Test
	void createRejectsNegativeAmount() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/expenses")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "expense_date": "2026-05-12",
								  "category": "cleaning",
								  "amount": -1
								}
								"""))
				.andExpect(status().isBadRequest());

		assertThat(propertyExpenseRepository.findAll()).isEmpty();
	}

	@Test
	void createRejectsMissingCategory() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/expenses")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "expense_date": "2026-05-12",
								  "amount": 750000.00
								}
								"""))
				.andExpect(status().isBadRequest());

		assertThat(propertyExpenseRepository.findAll()).isEmpty();
	}

	@Test
	void createHandlesPropertyNotFound() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/expenses")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000999",
								  "expense_date": "2026-05-12",
								  "category": "cleaning",
								  "amount": 750000.00
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));

		assertThat(propertyExpenseRepository.findAll()).isEmpty();
	}
}
