package com.propertybilling.integration.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.Property;
import com.propertybilling.entity.PropertyExpense;
import com.propertybilling.entity.User;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.PropertyExpenseRepository;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for property expense deletion across HTTP, persistence, and token validation.
 */
class ExpenseDeleteIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyExpenseRepository propertyExpenseRepository;
	private final PropertyRepository propertyRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;
	private Property property;

	@Autowired
	ExpenseDeleteIntegrationTest(
			MockMvc mockMvc,
			PropertyExpenseRepository propertyExpenseRepository,
			PropertyRepository propertyRepository,
			UserRepository userRepository,
			JwtTokenService jwtTokenService
	) {
		this.mockMvc = mockMvc;
		this.propertyExpenseRepository = propertyExpenseRepository;
		this.propertyRepository = propertyRepository;
		this.userRepository = userRepository;
		this.jwtTokenService = jwtTokenService;
	}

	@BeforeEach
	void setUp() {
		user = userRepository.save(new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"Admin User",
				"admin@example.com",
				"password-hash",
				"admin"
		));
		property = propertyRepository.save(new Property(
				UUID.fromString("00000000-0000-0000-0000-000000000101"),
				"Green Residence",
				null,
				true,
				OffsetDateTime.parse("2026-05-20T09:00:00Z"),
				OffsetDateTime.parse("2026-05-20T09:00:00Z")
		));
		propertyExpenseRepository.save(buildExpense());
	}

	@Test
	void deleteRemovesExpense() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(delete("/api/v1/expenses/00000000-0000-0000-0000-000000000201")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		assertThat(propertyExpenseRepository.findById(UUID.fromString(
				"00000000-0000-0000-0000-000000000201"
		))).isEmpty();
	}

	@Test
	void deleteHandlesExpenseNotFound() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(delete("/api/v1/expenses/00000000-0000-0000-0000-000000000999")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));

		assertThat(propertyExpenseRepository.findById(UUID.fromString(
				"00000000-0000-0000-0000-000000000201"
		))).isPresent();
	}

	private PropertyExpense buildExpense() {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-20T09:00:00Z");
		return new PropertyExpense(
				UUID.fromString("00000000-0000-0000-0000-000000000201"),
				property,
				LocalDate.parse("2026-05-12"),
				"cleaning",
				"750000.00",
				"Monthly cleaning fee",
				null,
				timestamp,
				timestamp
		);
	}
}
