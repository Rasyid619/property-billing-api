package com.propertybilling.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.config.SecurityConfig;
import com.propertybilling.dto.property.PropertyIndexElement;
import com.propertybilling.dto.property.PropertyIndexResponse;
import com.propertybilling.dto.property.PropertyShowResponse;
import com.propertybilling.entity.User;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.exception.InvalidAccessTokenException;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.PropertyService;
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

	@Nested
	/*
	 * Web-layer tests for creating properties.
	 */
	class CreateProperty {

		@Test
		void returnsCreated() throws Exception {
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

			mockMvc.perform(post("/api/v1/properties")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Green Residence",
									  "address": "Bekasi"
									}
									"""))
					.andExpect(status().isCreated())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(propertyService, times(1)).createProperty(Mockito.argThat(request ->
					"Green Residence".equals(request.name()) && "Bekasi".equals(request.address())
			));
		}

		@Test
		void rejectsBlankName() throws Exception {
			mockMvc.perform(post("/api/v1/properties")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": " ",
									  "address": "Bekasi"
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, propertyService);
		}

		@Test
		void rejectsMalformedRequestBody() throws Exception {
			mockMvc.perform(post("/api/v1/properties")
							.header("Authorization", "Bearer access-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Green Residence",
									}
									"""))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(""));

			verifyNoInteractions(authService, propertyService);
		}

		@Test
		void rejectsMissingAuthorizationHeader() throws Exception {
			mockMvc.perform(post("/api/v1/properties")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Green Residence",
									  "address": "Bekasi"
									}
									"""))
					.andExpect(status().isUnauthorized());

			verifyNoInteractions(authService, propertyService);
		}

		@Test
		void rejectsInvalidAccessToken() throws Exception {
			when(authService.authenticateAccessToken("Bearer invalid-token")).thenThrow(new InvalidAccessTokenException());

			mockMvc.perform(post("/api/v1/properties")
							.header("Authorization", "Bearer invalid-token")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Green Residence",
									  "address": "Bekasi"
									}
									"""))
					.andExpect(status().isUnauthorized());

			verify(authService, times(1)).authenticateAccessToken("Bearer invalid-token");
			verify(propertyService, never()).createProperty(Mockito.any());
		}
	}

	@Nested
	/*
	 * Web-layer tests for showing one property.
	 */
	class ShowProperty {

		@Test
		void returnsProperty() throws Exception {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(propertyService.getProperty(propertyId)).thenReturn(new PropertyShowResponse(
					propertyId,
					"Green Residence",
					"Bekasi",
					true
			));

			mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000101")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000101"))
					.andExpect(jsonPath("$.name").value("Green Residence"))
					.andExpect(jsonPath("$.address").value("Bekasi"))
					.andExpect(jsonPath("$.active").value(true))
					.andExpect(jsonPath("$.created_at").doesNotExist())
					.andExpect(jsonPath("$.updated_at").doesNotExist());

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(propertyService, times(1)).getProperty(propertyId);
		}

		@Test
		void returnsNotFoundWhenPropertyDoesNotExist() throws Exception {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(propertyService.getProperty(propertyId)).thenThrow(new PropertyNotFoundException());

			mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000999")
							.header("Authorization", "Bearer access-token"))
					.andExpect(status().isNotFound())
					.andExpect(content().string(""));

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(propertyService, times(1)).getProperty(propertyId);
		}

		@Test
		void rejectsMissingAuthorizationHeader() throws Exception {
			mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000101"))
					.andExpect(status().isUnauthorized());

			verifyNoInteractions(authService, propertyService);
		}

		@Test
		void rejectsInvalidAccessToken() throws Exception {
			when(authService.authenticateAccessToken("Bearer invalid-token")).thenThrow(new InvalidAccessTokenException());

			mockMvc.perform(get("/api/v1/properties/00000000-0000-0000-0000-000000000101")
							.header("Authorization", "Bearer invalid-token"))
					.andExpect(status().isUnauthorized());

			verify(authService, times(1)).authenticateAccessToken("Bearer invalid-token");
			verify(propertyService, never()).getProperty(Mockito.any());
		}
	}

	@Nested
	/*
	 * Web-layer tests for listing properties.
	 */
	class IndexProperties {

		@Test
		void returnsProperties() throws Exception {
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

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(propertyService, times(1)).listProperties(0, 100, null, null);
		}

		@Test
		void passesStatusFilter() throws Exception {
			when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
			when(propertyService.listProperties(0, 100, null, "inactive")).thenReturn(new PropertyIndexResponse(0, List.of()));

			mockMvc.perform(get("/api/v1/properties")
							.header("Authorization", "Bearer access-token")
							.param("status", "inactive"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.count").value(0))
					.andExpect(jsonPath("$.properties").isArray());

			verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
			verify(propertyService, times(1)).listProperties(0, 100, null, "inactive");
		}

		@Test
		void rejectsMissingAuthorizationHeader() throws Exception {
			mockMvc.perform(get("/api/v1/properties"))
					.andExpect(status().isUnauthorized());

			verifyNoInteractions(authService, propertyService);
		}

		@Test
		void rejectsInvalidAccessToken() throws Exception {
			when(authService.authenticateAccessToken("Bearer invalid-token")).thenThrow(new InvalidAccessTokenException());

			mockMvc.perform(get("/api/v1/properties")
							.header("Authorization", "Bearer invalid-token"))
					.andExpect(status().isUnauthorized());

			verify(authService, times(1)).authenticateAccessToken("Bearer invalid-token");
			verify(propertyService, never())
					.listProperties(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.any());
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
