package com.propertybilling.integration.cashbalance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.constant.InvoiceStatus;
import com.propertybilling.constant.PaymentMethod;
import com.propertybilling.entity.CashBalance;
import com.propertybilling.entity.Invoice;
import com.propertybilling.entity.Payment;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.PropertyExpense;
import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.Unit;
import com.propertybilling.entity.User;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.CashBalanceRepository;
import com.propertybilling.repository.InvoiceRepository;
import com.propertybilling.repository.PaymentRepository;
import com.propertybilling.repository.PropertyExpenseRepository;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.UnitRepository;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for monthly cash-balance closing across HTTP, persistence, and token validation.
 */
class CashBalanceCloseMonthIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final CashBalanceRepository cashBalanceRepository;
	private final InvoiceRepository invoiceRepository;
	private final PaymentRepository paymentRepository;
	private final PropertyExpenseRepository propertyExpenseRepository;
	private final PropertyRepository propertyRepository;
	private final TenantRepository tenantRepository;
	private final UnitRepository unitRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;
	private Property property;
	private Property otherProperty;
	private Invoice invoice;
	private Invoice otherInvoice;

	@Autowired
	CashBalanceCloseMonthIntegrationTest(
			MockMvc mockMvc,
			CashBalanceRepository cashBalanceRepository,
			InvoiceRepository invoiceRepository,
			PaymentRepository paymentRepository,
			PropertyExpenseRepository propertyExpenseRepository,
			PropertyRepository propertyRepository,
			TenantRepository tenantRepository,
			UnitRepository unitRepository,
			UserRepository userRepository,
			JwtTokenService jwtTokenService
	) {
		this.mockMvc = mockMvc;
		this.cashBalanceRepository = cashBalanceRepository;
		this.invoiceRepository = invoiceRepository;
		this.paymentRepository = paymentRepository;
		this.propertyExpenseRepository = propertyExpenseRepository;
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
		property = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000101",
				"Green Residence"
		));
		otherProperty = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000102",
				"Blue Terrace"
		));
		Unit unit = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000201",
				property,
				"A-101"
		));
		Unit otherUnit = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000202",
				otherProperty,
				"B-101"
		));
		Tenant tenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000301",
				"Budi"
		));
		Tenant otherTenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000302",
				"Andi"
		));
		invoice = invoiceRepository.save(buildInvoice(
				"00000000-0000-0000-0000-000000000401",
				unit,
				tenant,
				"2026-05-01",
				"INV-202605-A101"
		));
		otherInvoice = invoiceRepository.save(buildInvoice(
				"00000000-0000-0000-0000-000000000402",
				otherUnit,
				otherTenant,
				"2026-05-01",
				"INV-202605-B101"
		));
	}

	@Test
	void closeMonthStoresCashBalanceWithPreviousClosingBalance() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);
		cashBalanceRepository.saveAndFlush(new CashBalance(
				UUID.fromString("00000000-0000-0000-0000-000000000901"),
				property,
				LocalDate.parse("2026-04-01"),
				"0.00",
				"250000.00",
				"150000.00",
				"100000.00"
		));
		saveCashBalanceSourceRows();

		mockMvc.perform(post("/api/v1/cash-balances/close-month")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "month": "2026-05-01"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		CashBalance cashBalance = cashBalanceRepository
				.findByPropertyIdAndMonth(property.getId(), LocalDate.parse("2026-05-01"))
				.orElseThrow();
		assertMoney(cashBalance.getOpeningBalance(), "100000.00");
		assertMoney(cashBalance.getTotalIncome(), "1500000.00");
		assertMoney(cashBalance.getTotalExpense(), "400000.00");
		assertMoney(cashBalance.getClosingBalance(), "1200000.00");
	}

	@Test
	void closeMonthUsesZeroOpeningWhenPreviousBalanceDoesNotExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/cash-balances/close-month")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "month": "2026-07-01"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		CashBalance cashBalance = cashBalanceRepository
				.findByPropertyIdAndMonth(property.getId(), LocalDate.parse("2026-07-01"))
				.orElseThrow();
		assertMoney(cashBalance.getOpeningBalance(), "0.00");
		assertMoney(cashBalance.getTotalIncome(), "0.00");
		assertMoney(cashBalance.getTotalExpense(), "0.00");
		assertMoney(cashBalance.getClosingBalance(), "0.00");
	}

	@Test
	void closeMonthRejectsDuplicateClosing() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);
		cashBalanceRepository.saveAndFlush(new CashBalance(
				UUID.fromString("00000000-0000-0000-0000-000000000902"),
				property,
				LocalDate.parse("2026-05-01"),
				"0.00",
				"0.00",
				"0.00",
				"0.00"
		));

		mockMvc.perform(post("/api/v1/cash-balances/close-month")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "month": "2026-05-01"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(content().string(""));
	}

	@Test
	void closeMonthRejectsMonthThatIsNotFirstDay() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/cash-balances/close-month")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "month": "2026-05-02"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));
	}

	@Test
	void closeMonthHandlesPropertyNotFound() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(post("/api/v1/cash-balances/close-month")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000999",
								  "month": "2026-05-01"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));
	}

	private void saveCashBalanceSourceRows() {
		paymentRepository.save(new Payment(
				UUID.fromString("00000000-0000-0000-0000-000000000501"),
				invoice,
				"1000000.00",
				LocalDate.parse("2026-05-08"),
				PaymentMethod.BANK_TRANSFER.value(),
				null,
				null
		));
		paymentRepository.save(new Payment(
				UUID.fromString("00000000-0000-0000-0000-000000000502"),
				invoice,
				"500000.00",
				LocalDate.parse("2026-05-15"),
				PaymentMethod.CASH.value(),
				null,
				null
		));
		paymentRepository.save(new Payment(
				UUID.fromString("00000000-0000-0000-0000-000000000503"),
				invoice,
				"250000.00",
				LocalDate.parse("2026-06-01"),
				PaymentMethod.CASH.value(),
				null,
				null
		));
		paymentRepository.save(new Payment(
				UUID.fromString("00000000-0000-0000-0000-000000000504"),
				otherInvoice,
				"900000.00",
				LocalDate.parse("2026-05-10"),
				PaymentMethod.CASH.value(),
				null,
				null
		));
		propertyExpenseRepository.save(buildExpense(
				"00000000-0000-0000-0000-000000000601",
				property,
				"2026-05-12",
				"cleaning",
				"300000.00"
		));
		propertyExpenseRepository.save(buildExpense(
				"00000000-0000-0000-0000-000000000602",
				property,
				"2026-05-18",
				"repair",
				"100000.00"
		));
		propertyExpenseRepository.save(buildExpense(
				"00000000-0000-0000-0000-000000000603",
				property,
				"2026-06-01",
				"security",
				"75000.00"
		));
		propertyExpenseRepository.save(buildExpense(
				"00000000-0000-0000-0000-000000000604",
				otherProperty,
				"2026-05-18",
				"repair",
				"700000.00"
		));
	}

	private Property buildProperty(String id, String name) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-20T09:00:00Z");
		return new Property(
				UUID.fromString(id),
				name,
				null,
				true,
				timestamp,
				timestamp
		);
	}

	private Unit buildUnit(String id, Property property, String unitNumber) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-20T09:00:00Z");
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

	private Tenant buildTenant(String id, String name) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-20T09:00:00Z");
		return new Tenant(
				UUID.fromString(id),
				name,
				null,
				null,
				timestamp,
				timestamp
		);
	}

	private Invoice buildInvoice(String id, Unit unit, Tenant tenant, String billingMonth, String invoiceNumber) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-20T09:00:00Z");
		return new Invoice(
				UUID.fromString(id),
				unit,
				tenant,
				LocalDate.parse(billingMonth),
				invoiceNumber,
				"750000.00",
				LocalDate.parse("2026-05-10"),
				InvoiceStatus.UNPAID.value(),
				timestamp,
				timestamp
		);
	}

	private PropertyExpense buildExpense(
			String id,
			Property property,
			String expenseDate,
			String category,
			String amount
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-20T09:00:00Z");
		return new PropertyExpense(
				UUID.fromString(id),
				property,
				LocalDate.parse(expenseDate),
				category,
				amount,
				null,
				null,
				timestamp,
				timestamp
		);
	}

	private void assertMoney(String actual, String expected) {
		assertThat(new BigDecimal(actual)).isEqualByComparingTo(new BigDecimal(expected));
	}
}
