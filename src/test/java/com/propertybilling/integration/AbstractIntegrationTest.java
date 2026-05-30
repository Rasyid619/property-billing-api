package com.propertybilling.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
/*
 * Shared PostgreSQL Testcontainers base for integration tests that depend on real database behavior.
 *
 * A single container is started once and reused across all subclasses in the same JVM.
 * Shared cleanup removes cross-class data in foreign-key-safe order before each test.
 */
public abstract class AbstractIntegrationTest {

	static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	static {
		POSTGRES.start();
	}

	@DynamicPropertySource
	static void configure(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("INITIAL_ADMIN_ID", () -> "00000000-0000-0000-0000-000000000998");
		registry.add("INITIAL_ADMIN_NAME", () -> "Test Admin");
		registry.add("INITIAL_ADMIN_EMAIL", () -> "test-admin@example.com");
		registry.add("INITIAL_ADMIN_PASSWORD_HASH", () -> "$2a$10$abcdefghijklmnopqrstuv");
		registry.add("INITIAL_ADMIN_ROLE", () -> "admin");
		registry.add("app.jwt.secret", () -> "integration-test-secret");
	}

	@BeforeEach
	void cleanDatabase() {
		jdbcTemplate.update("DELETE FROM payments");
		jdbcTemplate.update("DELETE FROM invoices");
		jdbcTemplate.update("DELETE FROM unit_tenants");
		jdbcTemplate.update("DELETE FROM property_expenses");
		jdbcTemplate.update("DELETE FROM cash_balances");
		jdbcTemplate.update("DELETE FROM units");
		jdbcTemplate.update("DELETE FROM tenants");
		jdbcTemplate.update("DELETE FROM properties");
		jdbcTemplate.update("DELETE FROM users");
	}
}
