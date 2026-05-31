package com.propertybilling.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
				"tenant_unit_credits",
				"credit_applications",
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
	void invoiceStatusUsesDomainAndDefaultsToUnpaid() throws SQLException {
		try (var connection = dataSource.getConnection()) {
			try (var selectType = connection.prepareStatement("""
					SELECT domain_name
					FROM information_schema.columns
					WHERE table_schema = 'public'
					AND table_name = 'invoices'
					AND column_name = 'status'
					""");
					ResultSet typeResult = selectType.executeQuery()) {
				assertThat(typeResult.next()).isTrue();
				assertThat(typeResult.getString("domain_name")).isEqualTo("invoice_status");
			}

			try (var insertProperty = connection.prepareStatement("""
					INSERT INTO properties (
					    id,
					    name,
					    is_active
					)
					VALUES (
					    '00000000-0000-0000-0000-000000000501',
					    'Default Status Property',
					    TRUE
					)
					""")) {
				insertProperty.executeUpdate();
			}

			try (var insertUnit = connection.prepareStatement("""
					INSERT INTO units (
					    id,
					    property_id,
					    unit_number,
					    monthly_fee,
					    due_day,
					    is_active
					)
					VALUES (
					    '00000000-0000-0000-0000-000000000502',
					    '00000000-0000-0000-0000-000000000501',
					    'A-501',
					    '750000.00',
					    10,
					    TRUE
					)
					""")) {
				insertUnit.executeUpdate();
			}

			try (var insertTenant = connection.prepareStatement("""
					INSERT INTO tenants (
					    id,
					    name
					)
					VALUES (
					    '00000000-0000-0000-0000-000000000503',
					    'Budi'
					)
					""")) {
				insertTenant.executeUpdate();
			}

			try (var insertInvoice = connection.prepareStatement("""
					INSERT INTO invoices (
					    id,
					    unit_id,
					    tenant_id,
					    billing_month,
					    invoice_number,
					    amount,
					    due_date
					)
					VALUES (
					    '00000000-0000-0000-0000-000000000504',
					    '00000000-0000-0000-0000-000000000502',
					    '00000000-0000-0000-0000-000000000503',
					    '2026-05-01',
					    'INV-202605-A501',
					    '750000.00',
					    '2026-05-10'
					)
					""")) {
				insertInvoice.executeUpdate();
			}

			try (var selectStatus = connection.prepareStatement("""
					SELECT status::TEXT AS status
					FROM invoices
					WHERE id = '00000000-0000-0000-0000-000000000504'
					""");
					ResultSet statusResult = selectStatus.executeQuery()) {
				assertThat(statusResult.next()).isTrue();
				assertThat(statusResult.getString("status")).isEqualTo("unpaid");
			}
		}
	}

	@Test
	void paymentMethodUsesDomain() throws SQLException {
		try (var connection = dataSource.getConnection()) {
			try (var selectType = connection.prepareStatement("""
					SELECT domain_name
					FROM information_schema.columns
					WHERE table_schema = 'public'
					AND table_name = 'payments'
					AND column_name = 'payment_method'
					""");
					ResultSet typeResult = selectType.executeQuery()) {
				assertThat(typeResult.next()).isTrue();
				assertThat(typeResult.getString("domain_name")).isEqualTo("payment_method");
			}
		}
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
	void tenantContactValuesAreUniqueWhenProvided() throws SQLException {
		try (var connection = dataSource.getConnection()) {
			try (var insert = connection.prepareStatement("""
					INSERT INTO tenants (
					    id,
					    name,
					    phone,
					    email
					)
					VALUES (
					    ?,
					    ?,
					    ?,
					    ?
					)
					""")) {
				insert.setObject(1, java.util.UUID.fromString("00000000-0000-0000-0000-000000000301"));
				insert.setString(2, "Budi");
				insert.setString(3, "08123456789");
				insert.setString(4, "budi@example.com");
				insert.executeUpdate();

				insert.setObject(1, java.util.UUID.fromString("00000000-0000-0000-0000-000000000302"));
				insert.setString(2, "Andi");
				insert.setString(3, "08123456789");
				insert.setString(4, "andi@example.com");

				assertThatThrownBy(insert::executeUpdate).isInstanceOf(SQLException.class);

				insert.setObject(1, java.util.UUID.fromString("00000000-0000-0000-0000-000000000303"));
				insert.setString(2, "Sari");
				insert.setString(3, "08111111111");
				insert.setString(4, "budi@example.com");

				assertThatThrownBy(insert::executeUpdate).isInstanceOf(SQLException.class);
			}
		}
	}

	@Test
	void unitCanOnlyHaveOneActiveTenantAssignment() throws SQLException {
		try (var connection = dataSource.getConnection()) {
			try (var insertProperty = connection.prepareStatement("""
					INSERT INTO properties (
					    id,
					    name,
					    address,
					    is_active
					)
					VALUES (
					    '00000000-0000-0000-0000-000000000401',
					    'Green Residence',
					    'Bekasi',
					    TRUE
					)
					""")) {
				insertProperty.executeUpdate();
			}

			try (var insertUnit = connection.prepareStatement("""
					INSERT INTO units (
					    id,
					    property_id,
					    unit_number,
					    monthly_fee,
					    due_day,
					    is_active
					)
					VALUES (
					    '00000000-0000-0000-0000-000000000402',
					    '00000000-0000-0000-0000-000000000401',
					    'A-101',
					    '750000.00',
					    10,
					    TRUE
					)
					""")) {
				insertUnit.executeUpdate();
			}

			try (var insertTenant = connection.prepareStatement("""
					INSERT INTO tenants (
					    id,
					    name,
					    phone,
					    email
					)
					VALUES (
					    ?,
					    ?,
					    ?,
					    ?
					)
					""")) {
				insertTenant.setObject(1, java.util.UUID.fromString("00000000-0000-0000-0000-000000000403"));
				insertTenant.setString(2, "Budi");
				insertTenant.setString(3, "08222222222");
				insertTenant.setString(4, "budi-assignment@example.com");
				insertTenant.executeUpdate();

				insertTenant.setObject(1, java.util.UUID.fromString("00000000-0000-0000-0000-000000000404"));
				insertTenant.setString(2, "Andi");
				insertTenant.setString(3, "08333333333");
				insertTenant.setString(4, "andi-assignment@example.com");
				insertTenant.executeUpdate();
			}

			try (var insertAssignment = connection.prepareStatement("""
					INSERT INTO unit_tenants (
					    id,
					    unit_id,
					    tenant_id,
					    start_date,
					    end_date,
					    is_active
					)
					VALUES (
					    ?,
					    '00000000-0000-0000-0000-000000000402',
					    ?,
					    ?,
					    NULL,
					    TRUE
					)
					""")) {
				insertAssignment.setObject(1, java.util.UUID.fromString("00000000-0000-0000-0000-000000000405"));
				insertAssignment.setObject(2, java.util.UUID.fromString("00000000-0000-0000-0000-000000000403"));
				insertAssignment.setObject(3, java.time.LocalDate.parse("2026-05-01"));
				insertAssignment.executeUpdate();

				insertAssignment.setObject(1, java.util.UUID.fromString("00000000-0000-0000-0000-000000000406"));
				insertAssignment.setObject(2, java.util.UUID.fromString("00000000-0000-0000-0000-000000000404"));
				insertAssignment.setObject(3, java.time.LocalDate.parse("2026-06-01"));

				assertThatThrownBy(insertAssignment::executeUpdate).isInstanceOf(SQLException.class);
			}
		}
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
