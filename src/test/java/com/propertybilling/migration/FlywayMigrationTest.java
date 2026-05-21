package com.propertybilling.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.TreeSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
/*
 * Integration tests that verify the initial schema and seed migrations.
 */
class FlywayMigrationTest {

	@Container
	private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

	private final DataSource dataSource;

	@Autowired
	FlywayMigrationTest(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@DynamicPropertySource
	static void configureDatasource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("INITIAL_ADMIN_ID", () -> "00000000-0000-0000-0000-000000000999");
		registry.add("INITIAL_ADMIN_NAME", () -> "Initial Admin");
		registry.add("INITIAL_ADMIN_EMAIL", () -> "initial-admin@example.com");
		registry.add("INITIAL_ADMIN_PASSWORD_HASH", () -> "$2a$10$abcdefghijklmnopqrstuv");
		registry.add("INITIAL_ADMIN_ROLE", () -> "admin");
		registry.add("app.jwt.secret", () -> "migration-test-secret");
	}

	@Test
	void migrationCreatesInitialTables() throws SQLException {
		Set<String> tableNames = new TreeSet<>();

		try (var connection = dataSource.getConnection()) {
			DatabaseMetaData metadata = connection.getMetaData();

			try (ResultSet tables = metadata.getTables(null, "public", "%", new String[] { "TABLE" })) {
				while (tables.next()) {
					tableNames.add(tables.getString("TABLE_NAME"));
				}
			}
		}

		assertThat(tableNames).contains(
				"users",
				"properties",
				"units",
				"tenants",
				"unit_tenants",
				"invoices",
				"payments",
				"property_expenses",
				"cash_balances"
		);
	}

	@Test
	void triggerUpdatesUpdatedAtWhenRowChanges() throws SQLException {
		OffsetDateTime originalUpdatedAt = OffsetDateTime.parse("2026-05-17T00:00:00Z");
		OffsetDateTime changedUpdatedAt;

		try (var connection = dataSource.getConnection()) {
			try (var insert = connection.prepareStatement("""
					INSERT INTO properties (
					    id,
					    name,
					    address,
					    is_active,
					    created_at,
					    updated_at
					)
					VALUES (
					    '00000000-0000-0000-0000-000000000001',
					    'Green Residence',
					    'Bekasi',
					    TRUE,
					    ?,
					    ?
					)
					""")) {
				insert.setObject(1, originalUpdatedAt);
				insert.setObject(2, originalUpdatedAt);
				insert.executeUpdate();
			}

			try (var update = connection.prepareStatement("""
					UPDATE properties
					SET name = 'Green Residence Updated'
					WHERE id = '00000000-0000-0000-0000-000000000001'
					""")) {
				update.executeUpdate();
			}

			try (var select = connection.prepareStatement("""
					SELECT updated_at
					FROM properties
					WHERE id = '00000000-0000-0000-0000-000000000001'
					""");
					ResultSet result = select.executeQuery()) {
				result.next();
				changedUpdatedAt = result.getObject("updated_at", OffsetDateTime.class);
			}
		}

		assertThat(changedUpdatedAt).isAfter(originalUpdatedAt);
	}

	@Test
	void insertDefaultsPopulateCreatedAtAndUpdatedAt() throws SQLException {
		OffsetDateTime userCreatedAt;
		OffsetDateTime userUpdatedAt;
		OffsetDateTime propertyCreatedAt;
		OffsetDateTime propertyUpdatedAt;

		try (var connection = dataSource.getConnection()) {
			try (var insertUser = connection.prepareStatement("""
					INSERT INTO users (
					    id,
					    name,
					    email,
					    password_hash,
					    role
					)
					VALUES (
					    '00000000-0000-0000-0000-000000000111',
					    'Default Timestamp User',
					    'default-timestamp-user@example.com',
					    'hashed-password',
					    'admin'
					)
					""")) {
				insertUser.executeUpdate();
			}

			try (var insertProperty = connection.prepareStatement("""
					INSERT INTO properties (
					    id,
					    name,
					    address,
					    is_active
					)
					VALUES (
					    '00000000-0000-0000-0000-000000000112',
					    'Default Timestamp Property',
					    'Bekasi',
					    TRUE
					)
					""")) {
				insertProperty.executeUpdate();
			}

			try (var selectUser = connection.prepareStatement("""
					SELECT created_at, updated_at
					FROM users
					WHERE id = '00000000-0000-0000-0000-000000000111'
					""");
					ResultSet userResult = selectUser.executeQuery()) {
				userResult.next();
				userCreatedAt = userResult.getObject("created_at", OffsetDateTime.class);
				userUpdatedAt = userResult.getObject("updated_at", OffsetDateTime.class);
			}

			try (var selectProperty = connection.prepareStatement("""
					SELECT created_at, updated_at
					FROM properties
					WHERE id = '00000000-0000-0000-0000-000000000112'
					""");
					ResultSet propertyResult = selectProperty.executeQuery()) {
				propertyResult.next();
				propertyCreatedAt = propertyResult.getObject("created_at", OffsetDateTime.class);
				propertyUpdatedAt = propertyResult.getObject("updated_at", OffsetDateTime.class);
			}
		}

		assertThat(userCreatedAt).isNotNull();
		assertThat(userUpdatedAt).isNotNull();
		assertThat(propertyCreatedAt).isNotNull();
		assertThat(propertyUpdatedAt).isNotNull();
	}

	@Test
	void migrationSeedsInitialAdminFromEnvironmentBackedPlaceholders() throws SQLException {
		try (var connection = dataSource.getConnection();
				var select = connection.prepareStatement("""
						SELECT id, name, email, password_hash, role
						FROM users
						WHERE email = 'initial-admin@example.com'
						""");
				ResultSet result = select.executeQuery()) {
			result.next();

			assertThat(result.getObject("id")).hasToString("00000000-0000-0000-0000-000000000999");
			assertThat(result.getString("name")).isEqualTo("Initial Admin");
			assertThat(result.getString("email")).isEqualTo("initial-admin@example.com");
			assertThat(result.getString("password_hash")).isEqualTo("$2a$10$abcdefghijklmnopqrstuv");
			assertThat(result.getString("role")).isEqualTo("admin");
		}
	}
}
