package com.propertybilling.integration.payment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.entity.Invoice;
import com.propertybilling.entity.Payment;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.Unit;
import com.propertybilling.entity.User;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.InvoiceRepository;
import com.propertybilling.repository.PaymentRepository;
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
 * Integration tests for listing invoice payments across HTTP, persistence, and token validation.
 */
class PaymentIndexIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final InvoiceRepository invoiceRepository;
	private final JwtTokenService jwtTokenService;
	private final PaymentRepository paymentRepository;
	private final PropertyRepository propertyRepository;
	private final TenantRepository tenantRepository;
	private final UnitRepository unitRepository;
	private final UserRepository userRepository;
	private User user;
	private Invoice invoice;

	@Autowired
	PaymentIndexIntegrationTest(
			MockMvc mockMvc,
			InvoiceRepository invoiceRepository,
			JwtTokenService jwtTokenService,
			PaymentRepository paymentRepository,
			PropertyRepository propertyRepository,
			TenantRepository tenantRepository,
			UnitRepository unitRepository,
			UserRepository userRepository
	) {
		this.mockMvc = mockMvc;
		this.invoiceRepository = invoiceRepository;
		this.jwtTokenService = jwtTokenService;
		this.paymentRepository = paymentRepository;
		this.propertyRepository = propertyRepository;
		this.tenantRepository = tenantRepository;
		this.unitRepository = unitRepository;
		this.userRepository = userRepository;
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
		Property property = propertyRepository.save(buildProperty());
		Unit unit = unitRepository.save(buildUnit(property));
		Tenant tenant = tenantRepository.save(buildTenant());

		invoice = invoiceRepository.save(buildInvoice(
				"00000000-0000-0000-0000-000000000401",
				unit,
				tenant,
				"2026-05-01",
				"INV-202605-A101",
				"750000.00",
				"2026-05-10",
				"paid"
		));
	}

	@Test
	void indexReturnsPaymentsOrderedByPaymentDateAndCreationOrder() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);
		paymentRepository.save(buildPayment(
				"00000000-0000-0000-0000-000000000502",
				invoice,
				"250000.00",
				"2026-05-09",
				"cash",
				null,
				null
		));
		paymentRepository.save(buildPayment(
				"00000000-0000-0000-0000-000000000501",
				invoice,
				"500000.00",
				"2026-05-08",
				"bank_transfer",
				"BCA-123456",
				"Paid by tenant"
		));

		mockMvc.perform(get("/api/v1/invoices/00000000-0000-0000-0000-000000000401/payments")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(2))
				.andExpect(jsonPath("$.payments[0].id").value("00000000-0000-0000-0000-000000000501"))
				.andExpect(jsonPath("$.payments[0].invoice_id").value("00000000-0000-0000-0000-000000000401"))
				.andExpect(jsonPath("$.payments[0].amount").value(500000.00))
				.andExpect(jsonPath("$.payments[0].payment_date").value("2026-05-08"))
				.andExpect(jsonPath("$.payments[0].payment_method").value("bank_transfer"))
				.andExpect(jsonPath("$.payments[0].reference_number").value("BCA-123456"))
				.andExpect(jsonPath("$.payments[0].note").value("Paid by tenant"))
				.andExpect(jsonPath("$.payments[0].invoice_status").value("paid"))
				.andExpect(jsonPath("$.payments[1].id").value("00000000-0000-0000-0000-000000000502"))
				.andExpect(jsonPath("$.payments[1].amount").value(250000.00))
				.andExpect(jsonPath("$.payments[1].payment_date").value("2026-05-09"))
				.andExpect(jsonPath("$.payments[1].payment_method").value("cash"))
				.andExpect(jsonPath("$.payments[1].invoice_status").value("paid"));
	}

	@Test
	void indexReturnsEmptyPaymentsWhenInvoiceHasNoPayments() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/invoices/00000000-0000-0000-0000-000000000401/payments")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(0))
				.andExpect(jsonPath("$.payments").isEmpty());
	}

	@Test
	void indexReturnsNotFoundWhenInvoiceDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/invoices/00000000-0000-0000-0000-000000000999/payments")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));
	}

	private Payment buildPayment(
			String id,
			Invoice invoice,
			String amount,
			String paymentDate,
			String paymentMethod,
			String referenceNumber,
			String note
	) {
		return new Payment(
				UUID.fromString(id),
				invoice,
				amount,
				LocalDate.parse(paymentDate),
				paymentMethod,
				referenceNumber,
				note
		);
	}

	private Property buildProperty() {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Property(
				UUID.fromString("00000000-0000-0000-0000-000000000101"),
				"Green Residence",
				"Bekasi",
				true,
				timestamp,
				timestamp
		);
	}

	private Unit buildUnit(Property property) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Unit(
				UUID.fromString("00000000-0000-0000-0000-000000000201"),
				property,
				"A-101",
				"750000.00",
				10,
				true,
				timestamp,
				timestamp
		);
	}

	private Tenant buildTenant() {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Tenant(
				UUID.fromString("00000000-0000-0000-0000-000000000301"),
				"Budi",
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
