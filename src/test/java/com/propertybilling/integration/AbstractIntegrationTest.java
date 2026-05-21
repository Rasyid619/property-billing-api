package com.propertybilling.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
/*
 * Shared PostgreSQL Testcontainers base for integration tests that depend on real database behavior.
 *
 * A single container is started once and reused across all subclasses in the same JVM.
 * Each test class is responsible for cleaning its own data in @BeforeEach.
 */
public abstract class AbstractIntegrationTest {

	static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

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
}
