package com.propertybilling.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.config.SecurityConfig;
import com.propertybilling.dto.expense.ExpenseIndexElement;
import com.propertybilling.dto.expense.ExpenseIndexResponse;
import com.propertybilling.entity.User;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.PropertyExpenseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PropertyExpenseController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
/*
 * Web-layer tests for property expense endpoints.
 */
class PropertyExpenseControllerTest {

	private final MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private PropertyExpenseService propertyExpenseService;

	@Autowired
	PropertyExpenseControllerTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void createReturnsCreated() throws Exception {
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

		mockMvc.perform(post("/api/v1/expenses")
						.header("Authorization", "Bearer access-token")
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

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(propertyExpenseService, times(1)).createExpense(any());
	}

	@Test
	void createRejectsZeroAmount() throws Exception {
		mockMvc.perform(post("/api/v1/expenses")
						.header("Authorization", "Bearer access-token")
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "expense_date": "2026-05-12",
								  "category": "cleaning",
								  "amount": 0
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, propertyExpenseService);
	}

	@Test
	void createRejectsMissingCategory() throws Exception {
		mockMvc.perform(post("/api/v1/expenses")
						.header("Authorization", "Bearer access-token")
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "expense_date": "2026-05-12",
								  "amount": 750000.00
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, propertyExpenseService);
	}

	@Test
	void createRejectsUnsupportedCategory() throws Exception {
		mockMvc.perform(post("/api/v1/expenses")
						.header("Authorization", "Bearer access-token")
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "expense_date": "2026-05-12",
								  "category": "party",
								  "amount": 750000.00
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, propertyExpenseService);
	}

	@Test
	void indexReturnsExpenses() throws Exception {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		when(propertyExpenseService.listExpenses(propertyId, "2026-05", 0, 100)).thenReturn(
				new ExpenseIndexResponse(1, List.of(new ExpenseIndexElement(
						UUID.fromString("00000000-0000-0000-0000-000000000201"),
						propertyId,
						LocalDate.parse("2026-05-12"),
						"cleaning",
						new BigDecimal("750000.00"),
						"Monthly cleaning fee"
				)))
		);

		mockMvc.perform(get("/api/v1/expenses")
						.header("Authorization", "Bearer access-token")
						.param("property_id", propertyId.toString())
						.param("month", "2026-05"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.expenses[0].id").value("00000000-0000-0000-0000-000000000201"))
				.andExpect(jsonPath("$.expenses[0].property_id").value(propertyId.toString()))
				.andExpect(jsonPath("$.expenses[0].expense_date").value("2026-05-12"))
				.andExpect(jsonPath("$.expenses[0].category").value("cleaning"))
				.andExpect(jsonPath("$.expenses[0].amount").value(750000.00))
				.andExpect(jsonPath("$.expenses[0].description").value("Monthly cleaning fee"))
				.andExpect(jsonPath("$.expenses[0].created_at").doesNotExist())
				.andExpect(jsonPath("$.expenses[0].updated_at").doesNotExist());

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(propertyExpenseService, times(1)).listExpenses(propertyId, "2026-05", 0, 100);
	}

	@Test
	void updateReturnsNoContent() throws Exception {
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

		mockMvc.perform(put("/api/v1/expenses/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer access-token")
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "expense_date": "2026-05-15",
								  "category": "repair",
								  "amount": 1250000.00,
								  "description": "Gate repair"
								}
								"""))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(propertyExpenseService, times(1)).updateExpense(any(), any());
	}

	@Test
	void updateRejectsZeroAmount() throws Exception {
		mockMvc.perform(put("/api/v1/expenses/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer access-token")
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "expense_date": "2026-05-15",
								  "category": "repair",
								  "amount": 0
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, propertyExpenseService);
	}

	@Test
	void updateRejectsMissingCategory() throws Exception {
		mockMvc.perform(put("/api/v1/expenses/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer access-token")
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "expense_date": "2026-05-15",
								  "amount": 1250000.00
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, propertyExpenseService);
	}

	@Test
	void updateRejectsUnsupportedCategory() throws Exception {
		mockMvc.perform(put("/api/v1/expenses/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer access-token")
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "expense_date": "2026-05-15",
								  "category": "party",
								  "amount": 1250000.00
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, propertyExpenseService);
	}

	@Test
	void rejectsMissingPropertyId() throws Exception {
		mockMvc.perform(get("/api/v1/expenses")
						.header("Authorization", "Bearer access-token"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, propertyExpenseService);
	}

	@Test
	void rejectsInvalidMonth() throws Exception {
		mockMvc.perform(get("/api/v1/expenses")
						.header("Authorization", "Bearer access-token")
						.param("property_id", "00000000-0000-0000-0000-000000000101")
						.param("month", "2026-13"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, propertyExpenseService);
	}

	@Test
	void rejectsInvalidOffset() throws Exception {
		mockMvc.perform(get("/api/v1/expenses")
						.header("Authorization", "Bearer access-token")
						.param("property_id", "00000000-0000-0000-0000-000000000101")
						.param("offset", "-1"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, propertyExpenseService);
	}

	@Test
	void rejectsInvalidLimit() throws Exception {
		mockMvc.perform(get("/api/v1/expenses")
						.header("Authorization", "Bearer access-token")
						.param("property_id", "00000000-0000-0000-0000-000000000101")
						.param("limit", "101"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, propertyExpenseService);
	}

	private User buildUser() {
		return new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"Admin User",
				"admin@example.com",
				"password-hash",
				"admin"
		);
	}
}
