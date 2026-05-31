package com.propertybilling.integration.invoice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
 * Integration tests for showing one invoice across HTTP, persistence, and token validation.
 */
class InvoiceShowIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final InvoiceRepository invoiceRepository;
	private final PropertyRepository propertyRepository;
	private final TenantRepository tenantRepository;
	private final UnitRepository unitRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;

	@Autowired
	InvoiceShowIntegrationTest(
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
		Unit unit = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000201",
				property,
				"A-101",
				"750000.00",
				10,
				true
		));
		Tenant tenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000301",
				"Budi"
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
	}

	@Test
	void showReturnsInvoiceDetail() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/invoices/00000000-0000-0000-0000-000000000401")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000401"))
				.andExpect(jsonPath("$.unit_id").value("00000000-0000-0000-0000-000000000201"))
				.andExpect(jsonPath("$.tenant_id").value("00000000-0000-0000-0000-000000000301"))
				.andExpect(jsonPath("$.billing_month").value("2026-05-01"))
				.andExpect(jsonPath("$.invoice_number").value("INV-202605-A101"))
				.andExpect(jsonPath("$.amount").value(750000.00))
				.andExpect(jsonPath("$.paid_amount").value(0.00))
				.andExpect(jsonPath("$.credit_applied_amount").value(0.00))
				.andExpect(jsonPath("$.amount_due").value(750000.00))
				.andExpect(jsonPath("$.due_date").value("2026-05-10"))
				.andExpect(jsonPath("$.status").value("unpaid"))
				.andExpect(jsonPath("$.created_at").doesNotExist())
				.andExpect(jsonPath("$.updated_at").doesNotExist());
	}

	@Test
	void showReturnsNotFoundWhenInvoiceDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/invoices/00000000-0000-0000-0000-000000000999")
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
