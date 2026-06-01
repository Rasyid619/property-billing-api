package com.propertybilling.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.config.SecurityConfig;
import com.propertybilling.entity.User;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.CashBalanceService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CashBalanceController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
/*
 * Web-layer tests for cash balance endpoints.
 */
class CashBalanceControllerTest {

	private final MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private CashBalanceService cashBalanceService;

	@Autowired
	CashBalanceControllerTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void closeMonthReturnsCreated() throws Exception {
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

		mockMvc.perform(post("/api/v1/cash-balances/close-month")
						.header("Authorization", "Bearer access-token")
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "month": "2026-05-01"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(cashBalanceService, times(1)).closeMonth(any());
	}

	@Test
	void closeMonthRejectsMissingPropertyId() throws Exception {
		mockMvc.perform(post("/api/v1/cash-balances/close-month")
						.header("Authorization", "Bearer access-token")
						.contentType("application/json")
						.content("""
								{
								  "month": "2026-05-01"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, cashBalanceService);
	}

	@Test
	void closeMonthRejectsMissingMonth() throws Exception {
		mockMvc.perform(post("/api/v1/cash-balances/close-month")
						.header("Authorization", "Bearer access-token")
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, cashBalanceService);
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
