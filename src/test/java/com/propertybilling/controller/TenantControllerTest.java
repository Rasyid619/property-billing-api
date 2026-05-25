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
import com.propertybilling.dto.tenant.TenantIndexElement;
import com.propertybilling.dto.tenant.TenantIndexResponse;
import com.propertybilling.entity.User;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.TenantService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TenantController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
/*
 * Web-layer tests for tenant endpoints.
 */
class TenantControllerTest {

	private final MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private TenantService tenantService;

	@Autowired
	TenantControllerTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Nested
	/*
	 * Web-layer tests for listing tenants.
	 */
	class IndexTenants {

		@Test
		void returnsTenants() throws Exception {
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(tenantService.listTenants(0, 100, null)).thenReturn(new TenantIndexResponse(
					1,
					List.of(new TenantIndexElement(
							UUID.fromString("00000000-0000-0000-0000-000000000301"),
							"Budi",
							"08123456789",
							"budi@example.com"
					))
			));

			mockMvc.perform(get("/api/v1/tenants")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.count").value(1))
					.andExpect(jsonPath("$.tenants[0].id").value("00000000-0000-0000-0000-000000000301"))
					.andExpect(jsonPath("$.tenants[0].name").value("Budi"))
					.andExpect(jsonPath("$.tenants[0].phone").value("08123456789"))
					.andExpect(jsonPath("$.tenants[0].email").value("budi@example.com"))
					.andExpect(jsonPath("$.tenants[0].created_at").doesNotExist())
					.andExpect(jsonPath("$.tenants[0].updated_at").doesNotExist());

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantService, times(1)).listTenants(0, 100, null);
		}

		@Test
		void passesSearchFilter() throws Exception {
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(tenantService.listTenants(10, 25, "budi")).thenReturn(new TenantIndexResponse(0, List.of()));

			mockMvc.perform(get("/api/v1/tenants")
							.header("Authorization", "Bearer access-token")
							.param("offset", "10")
							.param("limit", "25")
							.param("search", "budi"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.count").value(0))
					.andExpect(jsonPath("$.tenants").isArray());

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantService, times(1)).listTenants(10, 25, "budi");
		}

		@Test
		void rejectsInvalidLimit() throws Exception {
			mockMvc.perform(get("/api/v1/tenants")
							.header("Authorization", "Bearer access-token")
							.param("limit", "0"))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantService);
		}

		@Test
		void rejectsTooLongSearch() throws Exception {
			mockMvc.perform(get("/api/v1/tenants")
							.header("Authorization", "Bearer access-token")
							.param("search", "x".repeat(101)))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantService);
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
