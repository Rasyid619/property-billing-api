package com.propertybilling.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.config.SecurityConfig;
import com.propertybilling.dto.report.CashFlowReportResponse;
import com.propertybilling.entity.User;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.ReportService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
/*
 * Web-layer tests for reporting endpoints.
 */
class ReportControllerTest {

	private final MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private ReportService reportService;

	@Autowired
	ReportControllerTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void cashFlowReturnsReport() throws Exception {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		when(reportService.getCashFlowReport(propertyId, "2026-05")).thenReturn(new CashFlowReportResponse(
				propertyId,
				"2026-05",
				new BigDecimal("1500000.00"),
				new BigDecimal("400000.00"),
				new BigDecimal("1100000.00")
		));

		mockMvc.perform(get("/api/v1/reports/cash-flow")
						.header("Authorization", "Bearer access-token")
						.param("property_id", propertyId.toString())
						.param("month", "2026-05"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.property_id").value(propertyId.toString()))
				.andExpect(jsonPath("$.month").value("2026-05"))
				.andExpect(jsonPath("$.total_income").value(1500000.00))
				.andExpect(jsonPath("$.total_expense").value(400000.00))
				.andExpect(jsonPath("$.net_saving").value(1100000.00));

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(reportService, times(1)).getCashFlowReport(propertyId, "2026-05");
	}

	@Test
	void cashFlowRejectsMissingPropertyId() throws Exception {
		mockMvc.perform(get("/api/v1/reports/cash-flow")
						.header("Authorization", "Bearer access-token")
						.param("month", "2026-05"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, reportService);
	}

	@Test
	void cashFlowRejectsMissingMonth() throws Exception {
		mockMvc.perform(get("/api/v1/reports/cash-flow")
						.header("Authorization", "Bearer access-token")
						.param("property_id", "00000000-0000-0000-0000-000000000101"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, reportService);
	}

	@Test
	void cashFlowRejectsInvalidMonth() throws Exception {
		mockMvc.perform(get("/api/v1/reports/cash-flow")
						.header("Authorization", "Bearer access-token")
						.param("property_id", "00000000-0000-0000-0000-000000000101")
						.param("month", "2026-13"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, reportService);
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
