package com.propertybilling.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.config.SecurityConfig;
import com.propertybilling.dto.tenant.TenantIndexElement;
import com.propertybilling.dto.tenant.TenantIndexResponse;
import com.propertybilling.dto.tenant.TenantShowResponse;
import com.propertybilling.entity.User;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.exception.TenantContactConflictException;
import com.propertybilling.exception.TenantNotFoundException;
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
import org.springframework.http.MediaType;
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
	 * Web-layer tests for updating tenants.
	 */
	class UpdateTenant {

		@Test
		void returnsNoContent() throws Exception {
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

			mockMvc.perform(patch("/api/v1/tenants/00000000-0000-0000-0000-000000000301")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Andi",
									  "phone": "08111111111",
									  "email": "andi@example.com"
									}
									"""))
					.andExpect(status().isNoContent())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantService, times(1)).updateTenant(Mockito.eq(tenantId), Mockito.argThat(request ->
					"Andi".equals(request.name())
							&& "08111111111".equals(request.phone())
							&& "andi@example.com".equals(request.email())
			));
		}

		@Test
		void rejectsBlankName() throws Exception {
			mockMvc.perform(patch("/api/v1/tenants/00000000-0000-0000-0000-000000000301")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": " ",
									  "phone": "08111111111",
									  "email": "andi@example.com"
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantService);
		}

		@Test
		void rejectsInvalidEmail() throws Exception {
			mockMvc.perform(patch("/api/v1/tenants/00000000-0000-0000-0000-000000000301")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Andi",
									  "phone": "08111111111",
									  "email": "not-an-email"
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantService);
		}

		@Test
		void returnsNotFoundWhenTenantDoesNotExist() throws Exception {
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			Mockito.doThrow(new TenantNotFoundException())
					.when(tenantService)
					.updateTenant(Mockito.eq(tenantId), Mockito.any());

			mockMvc.perform(patch("/api/v1/tenants/00000000-0000-0000-0000-000000000999")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Andi",
									  "phone": "08111111111",
									  "email": "andi@example.com"
									}
									"""))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantService, times(1)).updateTenant(Mockito.eq(tenantId), Mockito.any());
		}

		@Test
		void returnsConflictWhenPhoneOrEmailAlreadyExists() throws Exception {
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			Mockito.doThrow(new TenantContactConflictException())
					.when(tenantService)
					.updateTenant(Mockito.eq(tenantId), Mockito.any());

			mockMvc.perform(patch("/api/v1/tenants/00000000-0000-0000-0000-000000000301")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Andi",
									  "phone": "08111111111",
									  "email": "andi@example.com"
									}
									"""))
					.andExpect(status().isConflict())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantService, times(1)).updateTenant(Mockito.eq(tenantId), Mockito.any());
		}
	}

	@Nested
	/*
	 * Web-layer tests for showing one tenant.
	 */
	class ShowTenant {

		@Test
		void returnsTenant() throws Exception {
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(tenantService.getTenant(tenantId)).thenReturn(new TenantShowResponse(
					tenantId,
					"Budi",
					"08123456789",
					"budi@example.com"
			));

			mockMvc.perform(get("/api/v1/tenants/00000000-0000-0000-0000-000000000301")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000301"))
					.andExpect(jsonPath("$.name").value("Budi"))
					.andExpect(jsonPath("$.phone").value("08123456789"))
					.andExpect(jsonPath("$.email").value("budi@example.com"))
					.andExpect(jsonPath("$.created_at").doesNotExist())
					.andExpect(jsonPath("$.updated_at").doesNotExist());

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantService, times(1)).getTenant(tenantId);
		}

		@Test
		void returnsNotFoundWhenTenantDoesNotExist() throws Exception {
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(tenantService.getTenant(tenantId)).thenThrow(new TenantNotFoundException());

			mockMvc.perform(get("/api/v1/tenants/00000000-0000-0000-0000-000000000999")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantService, times(1)).getTenant(tenantId);
		}
	}

	@Nested
	/*
	 * Web-layer tests for creating tenants.
	 */
	class CreateTenant {

		@Test
		void returnsCreated() throws Exception {
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

			mockMvc.perform(post("/api/v1/tenants")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Budi",
									  "phone": "08123456789",
									  "email": "budi@example.com"
									}
									"""))
					.andExpect(status().isCreated())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantService, times(1)).createTenant(Mockito.argThat(request ->
					"Budi".equals(request.name())
							&& "08123456789".equals(request.phone())
							&& "budi@example.com".equals(request.email())
			));
		}

		@Test
		void rejectsBlankName() throws Exception {
			mockMvc.perform(post("/api/v1/tenants")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": " ",
									  "phone": "08123456789",
									  "email": "budi@example.com"
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantService);
		}

		@Test
		void rejectsInvalidEmail() throws Exception {
			mockMvc.perform(post("/api/v1/tenants")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Budi",
									  "phone": "08123456789",
									  "email": "not-an-email"
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantService);
		}

		@Test
		void rejectsMalformedRequestBody() throws Exception {
			mockMvc.perform(post("/api/v1/tenants")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Budi",
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantService);
		}

		@Test
		void returnsConflictWhenPhoneOrEmailAlreadyExists() throws Exception {
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			Mockito.doThrow(new TenantContactConflictException())
					.when(tenantService)
					.createTenant(Mockito.any());

			mockMvc.perform(post("/api/v1/tenants")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Budi",
									  "phone": "08123456789",
									  "email": "budi@example.com"
									}
									"""))
					.andExpect(status().isConflict())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantService, times(1)).createTenant(Mockito.any());
		}
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
