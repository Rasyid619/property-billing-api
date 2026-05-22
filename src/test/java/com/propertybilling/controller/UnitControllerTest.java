package com.propertybilling.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.config.SecurityConfig;
import com.propertybilling.dto.unit.UnitIndexElement;
import com.propertybilling.dto.unit.UnitIndexResponse;
import com.propertybilling.entity.User;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.exception.UnitNumberConflictException;
import com.propertybilling.exception.UnitNotFoundException;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.UnitService;
import java.math.BigDecimal;
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

@WebMvcTest(UnitController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
/*
 * Web-layer tests for unit endpoints.
 */
class UnitControllerTest {

	private final MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private UnitService unitService;

	@Autowired
	UnitControllerTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Nested
	/*
	 * Web-layer tests for creating units.
	 */
	class CreateUnit {

		@Test
		void returnsCreated() throws Exception {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

			mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "unit_number": "A-101",
									  "monthly_fee": "750000.00",
									  "due_day": 10
									}
									"""))
					.andExpect(status().isCreated())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(unitService, times(1)).createUnit(
					Mockito.eq(propertyId),
					Mockito.argThat(request ->
							"A-101".equals(request.unitNumber())
									&& "750000.00".equals(request.monthlyFee())
									&& request.dueDay() == 10
					)
			);
		}

		@Test
		void rejectsBlankUnitNumber() throws Exception {
			mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "unit_number": " ",
									  "monthly_fee": "750000.00",
									  "due_day": 10
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, unitService);
		}

		@Test
		void rejectsInvalidMonthlyFee() throws Exception {
			mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "unit_number": "A-101",
									  "monthly_fee": "0",
									  "due_day": 10
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, unitService);
		}

		@Test
		void rejectsInvalidDueDay() throws Exception {
			mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "unit_number": "A-101",
									  "monthly_fee": "750000.00",
									  "due_day": 29
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, unitService);
		}

		@Test
		void returnsNotFoundWhenPropertyDoesNotExist() throws Exception {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			Mockito.doThrow(new PropertyNotFoundException())
					.when(unitService)
					.createUnit(Mockito.eq(propertyId), Mockito.any());

			mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000999/units")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "unit_number": "A-101",
									  "monthly_fee": "750000.00",
									  "due_day": 10
									}
									"""))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(unitService, times(1)).createUnit(Mockito.eq(propertyId), Mockito.any());
		}

		@Test
		void returnsConflictWhenUnitNumberAlreadyExists() throws Exception {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			Mockito.doThrow(new UnitNumberConflictException())
					.when(unitService)
					.createUnit(Mockito.eq(propertyId), Mockito.any());

			mockMvc.perform(post("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "unit_number": "A-101",
									  "monthly_fee": "750000.00",
									  "due_day": 10
									}
									"""))
					.andExpect(status().isConflict())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(unitService, times(1)).createUnit(Mockito.eq(propertyId), Mockito.any());
		}
	}

	@Nested
	/*
	 * Web-layer tests for listing units by property.
	 */
	class IndexUnitsByProperty {

		@Test
		void returnsUnits() throws Exception {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(unitService.listUnitsByProperty(propertyId, 0, 100, null)).thenReturn(new UnitIndexResponse(
					1,
					List.of(new UnitIndexElement(
							UUID.fromString("00000000-0000-0000-0000-000000000201"),
							propertyId,
							"A-101",
							new BigDecimal("750000.00"),
							10,
							true
					))
			));

			mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.count").value(1))
					.andExpect(jsonPath("$.units[0].id").value("00000000-0000-0000-0000-000000000201"))
					.andExpect(jsonPath("$.units[0].property_id").value("00000000-0000-0000-0000-000000000101"))
					.andExpect(jsonPath("$.units[0].unit_number").value("A-101"))
					.andExpect(jsonPath("$.units[0].monthly_fee").value(750000.00))
					.andExpect(jsonPath("$.units[0].due_day").value(10))
					.andExpect(jsonPath("$.units[0].active").value(true))
					.andExpect(jsonPath("$.units[0].created_at").doesNotExist())
					.andExpect(jsonPath("$.units[0].updated_at").doesNotExist());

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(unitService, times(1)).listUnitsByProperty(propertyId, 0, 100, null);
		}

		@Test
		void passesStatusFilter() throws Exception {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(unitService.listUnitsByProperty(propertyId, 0, 100, "inactive"))
					.thenReturn(new UnitIndexResponse(0, List.of()));

			mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
							.header("Authorization", "Bearer access-token")
							.param("status", "inactive"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.count").value(0))
					.andExpect(jsonPath("$.units").isArray());

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(unitService, times(1)).listUnitsByProperty(propertyId, 0, 100, "inactive");
		}

		@Test
		void returnsNotFoundWhenPropertyDoesNotExist() throws Exception {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(unitService.listUnitsByProperty(propertyId, 0, 100, null)).thenThrow(new PropertyNotFoundException());

			mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000999/units")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(unitService, times(1)).listUnitsByProperty(propertyId, 0, 100, null);
		}

		@Test
		void rejectsInvalidLimit() throws Exception {
			mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000101/units")
							.header("Authorization", "Bearer access-token")
							.param("limit", "0"))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, unitService);
		}
	}

	@Nested
	/*
	 * Web-layer tests for deactivating units.
	 */
	class DeleteUnit {

		@Test
		void returnsNoContent() throws Exception {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

			mockMvc.perform(delete("/api/v1/units/00000000-0000-0000-0000-000000000201")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isNoContent())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(unitService, times(1)).deactivateUnit(unitId);
		}

		@Test
		void returnsNotFoundWhenUnitDoesNotExist() throws Exception {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			Mockito.doThrow(new UnitNotFoundException())
					.when(unitService)
					.deactivateUnit(unitId);

			mockMvc.perform(delete("/api/v1/units/00000000-0000-0000-0000-000000000999")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(unitService, times(1)).deactivateUnit(unitId);
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
