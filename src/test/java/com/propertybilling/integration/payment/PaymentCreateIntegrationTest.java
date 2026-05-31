package com.propertybilling.integration.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for recording invoice payments across HTTP, persistence, and token validation.
 */
class PaymentCreateIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final InvoiceRepository invoiceRepository;
	private final JdbcTemplate jdbcTemplate;
	private final JwtTokenService jwtTokenService;
	private final PropertyRepository propertyRepository;
	private final TenantRepository tenantRepository;
	private final UnitRepository unitRepository;
	private final UserRepository userRepository;
	private User user;

	@Autowired
	PaymentCreateIntegrationTest(
			MockMvc mockMvc,
			InvoiceRepository invoiceRepository,
			JdbcTemplate jdbcTemplate,
			JwtTokenService jwtTokenService,
			PropertyRepository propertyRepository,
			TenantRepository tenantRepository,
			UnitRepository unitRepository,
			UserRepository userRepository
	) {
		this.mockMvc = mockMvc;
		this.invoiceRepository = invoiceRepository;
		this.jdbcTemplate = jdbcTemplate;
		this.jwtTokenService = jwtTokenService;
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
		Unit unit = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000201",
				property,
				"A-101"
		));
		Tenant tenant = tenantRepository.save(buildTenant());

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
				"2026-06-01",
				"INV-202606-A101",
				"750000.00",
				"2026-06-10",
				"unpaid"
		));
	}

	@Test
	void createRecordsFullPaymentAndMarksInvoicePaid() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/invoices/00000000-0000-0000-0000-000000000401/payments")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "amount": 750000.00,
								  "payment_date": "2026-05-08",
								  "payment_method": "bank_transfer",
								  "reference_number": "BCA-123456",
								  "note": "Paid by tenant"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		assertThat(findInvoiceStatus("00000000-0000-0000-0000-000000000401")).isEqualTo("paid");
		assertThat(findPaymentAmounts("00000000-0000-0000-0000-000000000401"))
				.containsExactly("750000.00");
	}

	@Test
	void createAllocatesOverpaymentToNextOpenInvoice() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/invoices/00000000-0000-0000-0000-000000000401/payments")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "amount": 1000000.00,
								  "payment_date": "2026-05-08",
								  "payment_method": "bank_transfer"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		assertThat(findInvoiceStatus("00000000-0000-0000-0000-000000000401")).isEqualTo("paid");
		assertThat(findInvoiceStatus("00000000-0000-0000-0000-000000000402")).isEqualTo("partial");
		assertThat(findPaymentAmounts("00000000-0000-0000-0000-000000000401"))
				.containsExactly("750000.00");
		assertThat(findPaymentAmounts("00000000-0000-0000-0000-000000000402"))
				.containsExactly("250000.00");
	}

	@Test
	void createRejectsZeroAmount() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/invoices/00000000-0000-0000-0000-000000000401/payments")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "amount": 0,
								  "payment_date": "2026-05-08",
								  "payment_method": "bank_transfer"
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createRejectsUnsupportedPaymentMethod() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/invoices/00000000-0000-0000-0000-000000000401/payments")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "amount": 750000.00,
								  "payment_date": "2026-05-08",
								  "payment_method": "crypto"
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createReturnsNotFoundWhenInvoiceDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/invoices/00000000-0000-0000-0000-000000000999/payments")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "amount": 750000.00,
								  "payment_date": "2026-05-08",
								  "payment_method": "bank_transfer"
								}
								"""))
				.andExpect(status().isNotFound());
	}

	private String findInvoiceStatus(String invoiceId) {
		return jdbcTemplate.queryForObject(
				"SELECT status FROM invoices WHERE id = ?::uuid",
				String.class,
				invoiceId
		);
	}

	private List<String> findPaymentAmounts(String invoiceId) {
		return jdbcTemplate.queryForList(
				"""
						SELECT amount
						FROM payments
						WHERE invoice_id = ?::uuid
						ORDER BY created_at ASC, id ASC
						""",
				String.class,
				invoiceId
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

	private Unit buildUnit(String id, Property property, String unitNumber) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Unit(
				UUID.fromString(id),
				property,
				unitNumber,
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
