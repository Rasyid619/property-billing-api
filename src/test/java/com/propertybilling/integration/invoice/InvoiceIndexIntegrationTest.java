package com.propertybilling.integration.invoice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.Invoice;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.Unit;
import com.propertybilling.entity.User;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.InvoiceRepository;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.UnitRepository;
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
 * Integration tests for listing invoices across HTTP, persistence, and token validation.
 */
class InvoiceIndexIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final InvoiceRepository invoiceRepository;
	private final PropertyRepository propertyRepository;
	private final TenantRepository tenantRepository;
	private final UnitRepository unitRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;

	@Autowired
	InvoiceIndexIntegrationTest(
			MockMvc mockMvc,
			InvoiceRepository invoiceRepository,
			PropertyRepository propertyRepository,
			TenantRepository tenantRepository,
			UnitRepository unitRepository,
			UserRepository userRepository,
			JwtTokenService jwtTokenService
	) {
		this.mockMvc = mockMvc;
		this.invoiceRepository = invoiceRepository;
		this.propertyRepository = propertyRepository;
		this.tenantRepository = tenantRepository;
		this.unitRepository = unitRepository;
		this.userRepository = userRepository;
		this.jwtTokenService = jwtTokenService;
	}

	@BeforeEach
	void setUp() {
		invoiceRepository.deleteAll();
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
		Property property = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000101",
				"Green Residence",
				"Bekasi",
				true
		));
		Property otherProperty = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000102",
				"Blue Terrace",
				null,
				true
		));
		Unit unit = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000201",
				property,
				"A-101",
				"750000.00",
				10,
				true
		));
		Unit otherUnit = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000202",
				otherProperty,
				"B-101",
				"650000.00",
				8,
				true
		));
		Tenant tenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000301",
				"Budi"
		));
		Tenant otherTenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000302",
				"Andi"
		));

		invoiceRepository.save(buildInvoice(
				"00000000-0000-0000-0000-000000000401",
				unit,
				tenant,
				"2026-05-01",
				"INV-202605-A101",
				"750000.00",
				"2026-05-10",
				"unpaid"
		));
		invoiceRepository.save(buildInvoice(
				"00000000-0000-0000-0000-000000000402",
				unit,
				tenant,
				"2026-04-01",
				"INV-202604-A101",
				"750000.00",
				"2026-04-10",
				"paid"
		));
		invoiceRepository.save(buildInvoice(
				"00000000-0000-0000-0000-000000000403",
				otherUnit,
				otherTenant,
				"2026-05-01",
				"INV-202605-B101",
				"650000.00",
				"2026-05-08",
				"partial"
		));
	}

	@Test
	void indexReturnsInvoicesOrderedByNewestBillingMonth() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/invoices")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(3))
				.andExpect(jsonPath("$.invoices[0].id").value("00000000-0000-0000-0000-000000000403"))
				.andExpect(jsonPath("$.invoices[0].unit_id").value("00000000-0000-0000-0000-000000000202"))
				.andExpect(jsonPath("$.invoices[0].tenant_id").value("00000000-0000-0000-0000-000000000302"))
				.andExpect(jsonPath("$.invoices[0].billing_month").value("2026-05-01"))
				.andExpect(jsonPath("$.invoices[0].invoice_number").value("INV-202605-B101"))
				.andExpect(jsonPath("$.invoices[0].amount").value(650000.00))
				.andExpect(jsonPath("$.invoices[0].paid_amount").value(0.00))
				.andExpect(jsonPath("$.invoices[0].credit_applied_amount").value(0.00))
				.andExpect(jsonPath("$.invoices[0].amount_due").value(650000.00))
				.andExpect(jsonPath("$.invoices[0].due_date").value("2026-05-08"))
				.andExpect(jsonPath("$.invoices[0].status").value("partial"))
				.andExpect(jsonPath("$.invoices[0].created_at").doesNotExist())
				.andExpect(jsonPath("$.invoices[0].updated_at").doesNotExist())
				.andExpect(jsonPath("$.invoices[1].id").value("00000000-0000-0000-0000-000000000401"))
				.andExpect(jsonPath("$.invoices[2].id").value("00000000-0000-0000-0000-000000000402"));
	}

	@Test
	void indexFiltersInvoices() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/invoices")
						.header("Authorization", "Bearer " + accessToken)
						.param("property_id", "00000000-0000-0000-0000-000000000101")
						.param("unit_id", "00000000-0000-0000-0000-000000000201")
						.param("tenant_id", "00000000-0000-0000-0000-000000000301")
						.param("month", "2026-05")
						.param("status", "unpaid"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.invoices[0].id").value("00000000-0000-0000-0000-000000000401"));
	}

	@Test
	void indexRejectsInvalidMonth() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/invoices")
						.header("Authorization", "Bearer " + accessToken)
						.param("month", "2026-13"))
				.andExpect(status().isBadRequest());
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

	private Invoice buildInvoice(
			String id,
			Unit unit,
			Tenant tenant,
			String billingMonth,
			String invoiceNumber,
			String amount,
			String dueDate,
			String status
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Invoice(
				UUID.fromString(id),
				unit,
				tenant,
				LocalDate.parse(billingMonth),
				invoiceNumber,
				amount,
				LocalDate.parse(dueDate),
				status,
				timestamp,
				timestamp
		);
	}
}
