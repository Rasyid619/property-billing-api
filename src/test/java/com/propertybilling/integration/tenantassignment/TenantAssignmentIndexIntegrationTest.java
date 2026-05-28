package com.propertybilling.integration.tenantassignment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.Property;
import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.TenantAssignment;
import com.propertybilling.entity.Unit;
import com.propertybilling.entity.User;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.TenantAssignmentRepository;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.UnitRepository;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for tenant assignment history across HTTP, persistence, and token validation.
 */
class TenantAssignmentIndexIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyRepository propertyRepository;
	private final UnitRepository unitRepository;
	private final TenantRepository tenantRepository;
	private final TenantAssignmentRepository tenantAssignmentRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private Property property;
	private Unit unit;
	private Tenant firstTenant;
	private Tenant secondTenant;
	private User user;

	@Autowired
	TenantAssignmentIndexIntegrationTest(
			MockMvc mockMvc,
			PropertyRepository propertyRepository,
			UnitRepository unitRepository,
			TenantRepository tenantRepository,
			TenantAssignmentRepository tenantAssignmentRepository,
			UserRepository userRepository,
			JwtTokenService jwtTokenService
	) {
		this.mockMvc = mockMvc;
		this.propertyRepository = propertyRepository;
		this.unitRepository = unitRepository;
		this.tenantRepository = tenantRepository;
		this.tenantAssignmentRepository = tenantAssignmentRepository;
		this.userRepository = userRepository;
		this.jwtTokenService = jwtTokenService;
	}

	@BeforeEach
	void setUp() {
		tenantAssignmentRepository.deleteAll();
		unitRepository.deleteAll();
		tenantRepository.deleteAll();
		propertyRepository.deleteAll();
		userRepository.deleteAll();
		user = userRepository.save(new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"Admin User",
				"admin@example.com",
				"password-hash",
				"admin"
		));
		property = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000101",
				"Green Residence",
				"Bekasi",
				true
		));
		unit = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000201",
				property,
				"A-101",
				"750000.00",
				10,
				true
		));
		firstTenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000301",
				"Budi",
				"08123456789",
				"budi@example.com"
		));
		secondTenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000302",
				"Andi",
				"08111111111",
				"andi@example.com"
		));
	}

	@AfterEach
	void tearDown() {
		tenantAssignmentRepository.deleteAll();
	}

	@Test
	void indexReturnsTenantAssignmentHistoryNewestFirst() throws Exception {
		tenantAssignmentRepository.save(new TenantAssignment(
				UUID.fromString("00000000-0000-0000-0000-000000000401"),
				unit,
				firstTenant,
				LocalDate.parse("2026-01-01"),
				LocalDate.parse("2026-04-30"),
				false
		));
		tenantAssignmentRepository.save(new TenantAssignment(
				UUID.fromString("00000000-0000-0000-0000-000000000402"),
				unit,
				secondTenant,
				LocalDate.parse("2026-05-01"),
				null,
				true
		));
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
						.header("Authorization", "Bearer " + accessToken))
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
				.andExpect(jsonPath("$.tenant_assignments[1].tenant_id").value("00000000-0000-0000-0000-000000000301"))
				.andExpect(jsonPath("$.tenant_assignments[1].start_date").value("2026-01-01"))
				.andExpect(jsonPath("$.tenant_assignments[1].end_date").value("2026-04-30"))
				.andExpect(jsonPath("$.tenant_assignments[1].active").value(false));
	}

	@Test
	void indexReturnsEmptyHistoryWhenUnitHasNoAssignments() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(0))
				.andExpect(jsonPath("$.tenant_assignments").isArray());
	}

	@Test
	void indexReturnsNotFoundWhenUnitDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/units/00000000-0000-0000-0000-000000000999/tenant-assignments")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));
	}

	private Property buildProperty(
			String id,
			String name,
			String address,
			boolean active
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Property(
				UUID.fromString(id),
				name,
				address,
				active,
				timestamp,
				timestamp
		);
	}

	private Unit buildUnit(
			String id,
			Property property,
			String unitNumber,
			String monthlyFee,
			int dueDay,
			boolean active
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Unit(
				UUID.fromString(id),
				property,
				unitNumber,
				monthlyFee,
				dueDay,
				active,
				timestamp,
				timestamp
		);
	}

	private Tenant buildTenant(
			String id,
			String name,
			String phone,
			String email
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Tenant(
				UUID.fromString(id),
				name,
				phone,
				email,
				timestamp,
				timestamp
		);
	}
}
