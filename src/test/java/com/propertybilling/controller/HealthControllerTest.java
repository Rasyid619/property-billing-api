package com.propertybilling.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
/*
 * Web-layer tests for the health endpoint.
 */
class HealthControllerTest {

	private final MockMvc mockMvc;

	@Autowired
	HealthControllerTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void healthReturnsOk() throws Exception {
		mockMvc.perform(get("/api/v1/health"))
				.andExpect(status().isOk())
				.andExpect(content().string(""));
	}
}
