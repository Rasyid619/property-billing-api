package com.propertybilling.integration.tenantassignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for tenant assignment creation across HTTP, persistence, and token validation.
 */
class TenantAssignmentCreateIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final PropertyRepository propertyRepository;
	private final UnitRepository unitRepository;
	private final TenantRepository tenantRepository;
	private final TenantAssignmentRepository tenantAssignmentRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private Property property;
	private Unit unit;
	private Tenant tenant;
	private User user;

	@Autowired
	TenantAssignmentCreateIntegrationTest(
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
		tenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000301",
				"Budi",
				"08123456789",
				"budi@example.com"
		));
	}

	@AfterEach
	void tearDown() {
		tenantAssignmentRepository.deleteAll();
	}

	@Test
	void createPersistsActiveTenantAssignment() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "tenant_id": "00000000-0000-0000-0000-000000000301",
								  "start_date": "2026-05-01"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		TenantAssignment tenantAssignment = tenantAssignmentRepository.findActiveByUnitId(unit.getId()).orElseThrow();
		assertThat(tenantAssignment.getUnit().getId()).isEqualTo(unit.getId());
		assertThat(tenantAssignment.getTenant().getId()).isEqualTo(tenant.getId());
		assertThat(tenantAssignment.getStartDate()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(tenantAssignment.getEndDate()).isNull();
		assertThat(tenantAssignment.isActive()).isTrue();
	}

	@Test
	void createRejectsMissingTenantId() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "start_date": "2026-05-01"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));
	}

	@Test
	void createReturnsNotFoundWhenUnitDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/units/00000000-0000-0000-0000-000000000999/tenant-assignments")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "tenant_id": "00000000-0000-0000-0000-000000000301",
								  "start_date": "2026-05-01"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));
	}

	@Test
	void createReturnsNotFoundWhenTenantDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "tenant_id": "00000000-0000-0000-0000-000000000999",
								  "start_date": "2026-05-01"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));
	}

	@Test
	void createRejectsSecondActiveTenantAssignmentForSameUnit() throws Exception {
		tenantAssignmentRepository.save(new TenantAssignment(
				UUID.fromString("00000000-0000-0000-0000-000000000401"),
				unit,
				tenant,
				LocalDate.parse("2026-05-01"),
				null,
				true
		));
		Tenant secondTenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000302",
				"Andi",
				"08111111111",
				"andi@example.com"
		));
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/units/00000000-0000-0000-0000-000000000201/tenant-assignments")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "tenant_id": "00000000-0000-0000-0000-000000000302",
								  "start_date": "2026-06-01"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(content().string(""));

		assertThat(tenantAssignmentRepository.findActiveByUnitId(unit.getId()).orElseThrow().getTenant().getId())
				.isNotEqualTo(secondTenant.getId());
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
