package com.propertybilling.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.config.SecurityConfig;
import com.propertybilling.dto.property.PropertyIndexElement;
import com.propertybilling.dto.property.PropertyIndexResponse;
import com.propertybilling.entity.User;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.exception.InvalidAccessTokenException;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.PropertyService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PropertyController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
/*
 * Web-layer tests for property endpoints.
 */
class PropertyControllerTest {

	private final MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private PropertyService propertyService;

	@Autowired
	PropertyControllerTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void indexReturnsProperties() throws Exception {
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		when(propertyService.listProperties(0, 100, null, null)).thenReturn(new PropertyIndexResponse(
				1,
				List.of(new PropertyIndexElement(
						UUID.fromString("00000000-0000-0000-0000-000000000101"),
						"Green Residence",
						"Bekasi",
						true
				))
		));

		mockMvc.perform(get("/api/v1/properties")
						.header("Authorization", "Bearer access-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.properties[0].id").value("00000000-0000-0000-0000-000000000101"))
				.andExpect(jsonPath("$.properties[0].name").value("Green Residence"))
				.andExpect(jsonPath("$.properties[0].address").value("Bekasi"))
				.andExpect(jsonPath("$.properties[0].active").value(true))
				.andExpect(jsonPath("$.properties[0].created_at").doesNotExist())
				.andExpect(jsonPath("$.properties[0].updated_at").doesNotExist());
	}

	@Test
	void indexPassesStatusFilter() throws Exception {
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		when(propertyService.listProperties(0, 100, null, "inactive")).thenReturn(new PropertyIndexResponse(0, List.of()));

		mockMvc.perform(get("/api/v1/properties")
						.header("Authorization", "Bearer access-token")
						.param("status", "inactive"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(0))
				.andExpect(jsonPath("$.properties").isArray());
	}

	@Test
	void indexRejectsMissingAuthorizationHeader() throws Exception {
		mockMvc.perform(get("/api/v1/properties"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void indexRejectsInvalidAccessToken() throws Exception {
		when(authService.authenticateAccessToken("Bearer invalid-token")).thenThrow(new InvalidAccessTokenException());

		mockMvc.perform(get("/api/v1/properties")
						.header("Authorization", "Bearer invalid-token"))
				.andExpect(status().isUnauthorized());
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
