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
import com.propertybilling.dto.tenantassignment.TenantAssignmentCreateRequest;
import com.propertybilling.dto.tenantassignment.TenantAssignmentIndexElement;
import com.propertybilling.dto.tenantassignment.TenantAssignmentIndexResponse;
import com.propertybilling.dto.tenantassignment.TenantAssignmentMoveOutRequest;
import com.propertybilling.dto.tenantassignment.TenantAssignmentShowResponse;
import com.propertybilling.entity.User;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.exception.TenantAssignmentConflictException;
import com.propertybilling.exception.TenantAssignmentMoveOutDateException;
import com.propertybilling.exception.TenantAssignmentNotFoundException;
import com.propertybilling.exception.TenantNotFoundException;
import com.propertybilling.exception.UnitNotFoundException;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.TenantAssignmentService;
import java.time.LocalDate;
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
	 * Web-layer tests for creating tenant assignments.
	 */
	class CreateTenantAssignment {

		@Test
		void returnsCreated() throws Exception {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

			mockMvc.perform(post("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "tenant_id": "00000000-0000-0000-0000-000000000301",
									  "start_date": "2026-05-01"
									}
									"""))
					.andExpect(status().isCreated())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).createTenantAssignment(
					Mockito.eq(unitId),
					Mockito.argThat((TenantAssignmentCreateRequest request) ->
							tenantId.equals(request.tenantId())
									&& LocalDate.parse("2026-05-01").equals(request.startDate())
					)
			);
		}

		@Test
		void rejectsMissingTenantId() throws Exception {
			mockMvc.perform(post("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "start_date": "2026-05-01"
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantAssignmentService);
		}

		@Test
		void rejectsMissingStartDate() throws Exception {
			mockMvc.perform(post("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "tenant_id": "00000000-0000-0000-0000-000000000301"
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantAssignmentService);
		}

		@Test
		void returnsNotFoundWhenUnitDoesNotExist() throws Exception {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			Mockito.doThrow(new UnitNotFoundException())
					.when(tenantAssignmentService)
					.createTenantAssignment(Mockito.eq(unitId), Mockito.any());

			mockMvc.perform(post("/api/v1/units/00000000-0000-0000-0000-000000000999/tenant-assignments")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "tenant_id": "00000000-0000-0000-0000-000000000301",
									  "start_date": "2026-05-01"
									}
									"""))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).createTenantAssignment(Mockito.eq(unitId), Mockito.any());
		}

		@Test
		void returnsNotFoundWhenTenantDoesNotExist() throws Exception {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			Mockito.doThrow(new TenantNotFoundException())
					.when(tenantAssignmentService)
					.createTenantAssignment(Mockito.eq(unitId), Mockito.any());

			mockMvc.perform(post("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "tenant_id": "00000000-0000-0000-0000-000000000999",
									  "start_date": "2026-05-01"
									}
									"""))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).createTenantAssignment(Mockito.eq(unitId), Mockito.any());
		}

		@Test
		void returnsConflictWhenUnitAlreadyHasActiveTenantAssignment() throws Exception {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			Mockito.doThrow(new TenantAssignmentConflictException())
					.when(tenantAssignmentService)
					.createTenantAssignment(Mockito.eq(unitId), Mockito.any());

			mockMvc.perform(post("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "tenant_id": "00000000-0000-0000-0000-000000000301",
									  "start_date": "2026-05-01"
									}
									"""))
					.andExpect(status().isConflict())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).createTenantAssignment(Mockito.eq(unitId), Mockito.any());
		}
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

	@Nested
	/*
	 * Web-layer tests for listing tenant assignment history.
	 */
	class IndexTenantAssignments {

		@Test
		void returnsTenantAssignmentHistory() throws Exception {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(tenantAssignmentService.listTenantAssignments(unitId)).thenReturn(new TenantAssignmentIndexResponse(
					2,
					List.of(
							new TenantAssignmentIndexElement(
									UUID.fromString("00000000-0000-0000-0000-000000000402"),
									unitId,
									UUID.fromString("00000000-0000-0000-0000-000000000302"),
									LocalDate.parse("2026-05-01"),
									null,
									true
							),
							new TenantAssignmentIndexElement(
									UUID.fromString("00000000-0000-0000-0000-000000000401"),
									unitId,
									UUID.fromString("00000000-0000-0000-0000-000000000301"),
									LocalDate.parse("2026-01-01"),
									LocalDate.parse("2026-04-30"),
									false
							)
					)
			));

			mockMvc.perform(get("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.count").value(2))
					.andExpect(jsonPath("$.tenant_assignments[0].id").value("00000000-0000-0000-0000-000000000402"))
					.andExpect(jsonPath("$.tenant_assignments[0].unit_id").value("00000000-0000-0000-0000-000000000201"))
					.andExpect(jsonPath("$.tenant_assignments[0].tenant_id").value("00000000-0000-0000-0000-000000000302"))
					.andExpect(jsonPath("$.tenant_assignments[0].start_date").value("2026-05-01"))
					.andExpect(jsonPath("$.tenant_assignments[0].end_date").isEmpty())
					.andExpect(jsonPath("$.tenant_assignments[0].active").value(true))
					.andExpect(jsonPath("$.tenant_assignments[0].created_at").doesNotExist())
					.andExpect(jsonPath("$.tenant_assignments[0].updated_at").doesNotExist())
					.andExpect(jsonPath("$.tenant_assignments[1].id").value("00000000-0000-0000-0000-000000000401"))
					.andExpect(jsonPath("$.tenant_assignments[1].end_date").value("2026-04-30"))
					.andExpect(jsonPath("$.tenant_assignments[1].active").value(false));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).listTenantAssignments(unitId);
		}

		@Test
		void returnsNotFoundWhenUnitDoesNotExist() throws Exception {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(tenantAssignmentService.listTenantAssignments(unitId)).thenThrow(new UnitNotFoundException());

			mockMvc.perform(get("/api/v1/units/00000000-0000-0000-0000-000000000999/tenant-assignments")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).listTenantAssignments(unitId);
		}

		@Test
		void rejectsMissingToken() throws Exception {
			mockMvc.perform(get("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments"))
					.andExpect(status().isUnauthorized())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantAssignmentService);
		}
	}

	@Nested
	/*
	 * Web-layer tests for moving tenants out.
	 */
	class MoveOutTenantAssignment {

		@Test
		void returnsNoContent() throws Exception {
			UUID assignmentId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

			mockMvc.perform(patch("/api/v1/unit-tenant-assignments/00000000-0000-0000-0000-000000000401/move-out")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "end_date": "2026-05-31"
									}
									"""))
					.andExpect(status().isNoContent())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).moveOutTenantAssignment(
					Mockito.eq(assignmentId),
					Mockito.argThat((TenantAssignmentMoveOutRequest request) ->
							LocalDate.parse("2026-05-31").equals(request.endDate())
					)
			);
		}

		@Test
		void rejectsMissingEndDate() throws Exception {
			mockMvc.perform(patch("/api/v1/unit-tenant-assignments/00000000-0000-0000-0000-000000000401/move-out")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, tenantAssignmentService);
		}

		@Test
		void returnsBadRequestWhenEndDateIsBeforeStartDate() throws Exception {
			UUID assignmentId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			Mockito.doThrow(new TenantAssignmentMoveOutDateException())
					.when(tenantAssignmentService)
					.moveOutTenantAssignment(Mockito.eq(assignmentId), Mockito.any());

			mockMvc.perform(patch("/api/v1/unit-tenant-assignments/00000000-0000-0000-0000-000000000401/move-out")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "end_date": "2026-04-30"
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).moveOutTenantAssignment(Mockito.eq(assignmentId), Mockito.any());
		}

		@Test
		void returnsNotFoundWhenAssignmentDoesNotExist() throws Exception {
			UUID assignmentId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			Mockito.doThrow(new TenantAssignmentNotFoundException())
					.when(tenantAssignmentService)
					.moveOutTenantAssignment(Mockito.eq(assignmentId), Mockito.any());

			mockMvc.perform(patch("/api/v1/unit-tenant-assignments/00000000-0000-0000-0000-000000000999/move-out")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "end_date": "2026-05-31"
									}
									"""))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(tenantAssignmentService, times(1)).moveOutTenantAssignment(Mockito.eq(assignmentId), Mockito.any());
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
