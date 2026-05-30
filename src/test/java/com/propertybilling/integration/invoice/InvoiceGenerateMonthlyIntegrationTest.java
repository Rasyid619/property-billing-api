package com.propertybilling.integration.invoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.Invoice;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.TenantAssignment;
import com.propertybilling.entity.Unit;
import com.propertybilling.entity.User;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.InvoiceRepository;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.TenantAssignmentRepository;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.UnitRepository;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for monthly invoice generation across HTTP, persistence, and token validation.
 */
class InvoiceGenerateMonthlyIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final InvoiceRepository invoiceRepository;
	private final PropertyRepository propertyRepository;
	private final TenantAssignmentRepository tenantAssignmentRepository;
	private final TenantRepository tenantRepository;
	private final UnitRepository unitRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private Property property;
	private Unit activeUnit;
	private Tenant activeTenant;
	private User user;

	@Autowired
	InvoiceGenerateMonthlyIntegrationTest(
			MockMvc mockMvc,
			InvoiceRepository invoiceRepository,
			PropertyRepository propertyRepository,
			TenantAssignmentRepository tenantAssignmentRepository,
			TenantRepository tenantRepository,
			UnitRepository unitRepository,
			UserRepository userRepository,
			JwtTokenService jwtTokenService
	) {
		this.mockMvc = mockMvc;
		this.invoiceRepository = invoiceRepository;
		this.propertyRepository = propertyRepository;
		this.tenantAssignmentRepository = tenantAssignmentRepository;
		this.tenantRepository = tenantRepository;
		this.unitRepository = unitRepository;
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
		property = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000101",
				"Green Residence",
				"Bekasi",
				true
		));
		activeUnit = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000201",
				property,
				"A-101",
				"750000.00",
				10,
				true
		));
		Unit unitWithoutTenant = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000202",
				property,
				"A-102",
				"800000.00",
				15,
				true
		));
		Unit inactiveUnit = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000203",
				property,
				"A-103",
				"900000.00",
				20,
				false
		));
		activeTenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000301",
				"Budi"
		));
		Tenant inactiveUnitTenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000302",
				"Andi"
		));
		tenantAssignmentRepository.save(buildAssignment(
				"00000000-0000-0000-0000-000000000501",
				activeUnit,
				activeTenant,
				true,
				null
		));
		tenantAssignmentRepository.save(buildAssignment(
				"00000000-0000-0000-0000-000000000502",
				inactiveUnit,
				inactiveUnitTenant,
				true,
				null
		));
		assertThat(unitWithoutTenant.getId()).isNotNull();
	}

	@Test
	void generateMonthlyCreatesInvoicesForActiveUnitsWithActiveTenants() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/invoices/generate-monthly")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "billing_month": "2026-05-01"
								}
								"""))
				.andExpect(status().isCreated());

		List<Invoice> invoices = invoiceRepository.findAll()
				.stream()
				.sorted(Comparator.comparing(Invoice::getInvoiceNumber))
				.toList();
		assertThat(invoices).hasSize(1);
		assertThat(invoices.getFirst().getUnit().getId()).isEqualTo(activeUnit.getId());
		assertThat(invoices.getFirst().getTenant().getId()).isEqualTo(activeTenant.getId());
		assertThat(invoices.getFirst().getBillingMonth()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(invoices.getFirst().getInvoiceNumber())
				.isEqualTo("INV-202605-00000000000000000000000000000201");
		assertThat(invoices.getFirst().getAmount()).isEqualTo("750000.00");
		assertThat(invoices.getFirst().getDueDate()).isEqualTo(LocalDate.parse("2026-05-10"));
		assertThat(invoices.getFirst().getStatus()).isEqualTo("unpaid");
	}

	@Test
	void generateMonthlyReturnsConflictWhenEligibleInvoiceAlreadyExists() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);
		invoiceRepository.save(new Invoice(
				UUID.fromString("00000000-0000-0000-0000-000000000401"),
				activeUnit,
				activeTenant,
				LocalDate.parse("2026-05-01"),
				"INV-202605-EXISTING",
				"750000.00",
				LocalDate.parse("2026-05-10"),
				"unpaid"
		));

		mockMvc.perform(post("/api/v1/invoices/generate-monthly")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "billing_month": "2026-05-01"
								}
								"""))
				.andExpect(status().isConflict());

		assertThat(invoiceRepository.findAll()).hasSize(1);
	}

	@Test
	void generateMonthlyRejectsBillingMonthThatIsNotFirstDay() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/invoices/generate-monthly")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "billing_month": "2026-05-02"
								}
								"""))
				.andExpect(status().isBadRequest());

		assertThat(invoiceRepository.findAll()).isEmpty();
	}

	@Test
	void generateMonthlyReturnsNotFoundWhenPropertyDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/invoices/generate-monthly")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000999",
								  "billing_month": "2026-05-01"
								}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void generateMonthlyReturnsConflictWhenPropertyIsInactive() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);
		Property inactiveProperty = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000102",
				"Inactive Residence",
				null,
				false
		));

		mockMvc.perform(post("/api/v1/invoices/generate-monthly")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000102",
								  "billing_month": "2026-05-01"
								}
								"""))
				.andExpect(status().isConflict());

		assertThat(inactiveProperty.isActive()).isFalse();
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

	private Tenant buildTenant(String id, String name) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Tenant(
				UUID.fromString(id),
				name,
				null,
				null,
				timestamp,
				timestamp
		);
	}

	private TenantAssignment buildAssignment(
			String id,
			Unit unit,
			Tenant tenant,
			boolean active,
			LocalDate endDate
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new TenantAssignment(
				UUID.fromString(id),
				unit,
				tenant,
				LocalDate.parse("2026-01-01"),
				endDate,
				active,
				timestamp,
				timestamp
		);
	}
}
