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
import com.propertybilling.dto.tenantassignment.TenantAssignmentShowResponse;
import com.propertybilling.entity.User;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.exception.TenantAssignmentNotFoundException;
import com.propertybilling.exception.UnitNotFoundException;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.TenantAssignmentService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TenantAssignmentController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
/*
 * Web-layer tests for tenant assignment endpoints.
 */
class TenantAssignmentControllerTest {

	private final MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private TenantAssignmentService tenantAssignmentService;

	@Autowired
	TenantAssignmentControllerTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Nested
	/*
	 * Web-layer tests for active tenant lookup.
	 */
	class ShowActiveTenant {

		@Test
		void returnsActiveTenantAssignment() throws Exception {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(tenantAssignmentService.getActiveTenant(unitId)).thenReturn(new TenantAssignmentShowResponse(
					UUID.fromString("00000000-0000-0000-0000-000000000401"),
					unitId,
					UUID.fromString("00000000-0000-0000-0000-000000000301"),
					LocalDate.parse("2026-05-01"),
					null,
					true
			));

			mockMvc.perform(get("/api/v1/units/00000000-0000-0000-0000-000000000201/active-tenant")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000401"))
					.andExpect(jsonPath("$.unit_id").value("00000000-0000-0000-0000-000000000201"))
					.andExpect(jsonPath("$.tenant_id").value("00000000-0000-0000-0000-000000000301"))
					.andExpect(jsonPath("$.start_date").value("2026-05-01"))
					.andExpect(jsonPath("$.end_date").isEmpty())
					.andExpect(jsonPath("$.active").value(true))
					.andExpect(jsonPath("$.created_at").doesNotExist())
					.andExpect(jsonPath("$.updated_at").doesNotExist());

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).getActiveTenant(unitId);
		}

		@Test
		void returnsNotFoundWhenUnitDoesNotExist() throws Exception {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(tenantAssignmentService.getActiveTenant(unitId)).thenThrow(new UnitNotFoundException());

			mockMvc.perform(get("/api/v1/units/00000000-0000-0000-0000-000000000999/active-tenant")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).getActiveTenant(unitId);
		}

		@Test
		void returnsNotFoundWhenUnitHasNoActiveTenantAssignment() throws Exception {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(tenantAssignmentService.getActiveTenant(unitId)).thenThrow(new TenantAssignmentNotFoundException());

			mockMvc.perform(get("/api/v1/units/00000000-0000-0000-0000-000000000201/active-tenant")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).getActiveTenant(unitId);
		}

		@Test
		void rejectsMissingToken() throws Exception {
			mockMvc.perform(get("/api/v1/units/00000000-0000-0000-0000-000000000201/active-tenant"))
					.andExpect(status().isUnauthorized())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantAssignmentService);
		}
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
