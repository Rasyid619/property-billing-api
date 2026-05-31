package com.propertybilling.integration.report;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.constant.InvoiceStatus;
import com.propertybilling.constant.PaymentMethod;
import com.propertybilling.entity.CreditApplication;
import com.propertybilling.entity.Invoice;
import com.propertybilling.entity.Payment;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.PropertyExpense;
import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.TenantUnitCredit;
import com.propertybilling.entity.Unit;
import com.propertybilling.entity.User;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.CreditApplicationRepository;
import com.propertybilling.repository.InvoiceRepository;
import com.propertybilling.repository.PaymentRepository;
import com.propertybilling.repository.PropertyExpenseRepository;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.TenantUnitCreditRepository;
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
 * Integration tests for cash-flow reporting across HTTP, persistence, and token validation.
 */
class CashFlowReportIntegrationTest extends AbstractIntegrationTest {

	private final MockMvc mockMvc;
	private final CreditApplicationRepository creditApplicationRepository;
	private final InvoiceRepository invoiceRepository;
	private final PaymentRepository paymentRepository;
	private final PropertyExpenseRepository propertyExpenseRepository;
	private final PropertyRepository propertyRepository;
	private final TenantRepository tenantRepository;
	private final TenantUnitCreditRepository tenantUnitCreditRepository;
	private final UnitRepository unitRepository;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;
	private User user;
	private Property property;

	@Autowired
	CashFlowReportIntegrationTest(
			MockMvc mockMvc,
			CreditApplicationRepository creditApplicationRepository,
			InvoiceRepository invoiceRepository,
			PaymentRepository paymentRepository,
			PropertyExpenseRepository propertyExpenseRepository,
			PropertyRepository propertyRepository,
			TenantRepository tenantRepository,
			TenantUnitCreditRepository tenantUnitCreditRepository,
			UnitRepository unitRepository,
			UserRepository userRepository,
			JwtTokenService jwtTokenService
	) {
		this.mockMvc = mockMvc;
		this.creditApplicationRepository = creditApplicationRepository;
		this.invoiceRepository = invoiceRepository;
		this.paymentRepository = paymentRepository;
		this.propertyExpenseRepository = propertyExpenseRepository;
		this.propertyRepository = propertyRepository;
		this.tenantRepository = tenantRepository;
		this.tenantUnitCreditRepository = tenantUnitCreditRepository;
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
		Property otherProperty = propertyRepository.save(buildProperty(
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
		Invoice invoice = invoiceRepository.save(buildInvoice(
				"00000000-0000-0000-0000-000000000401",
				unit,
				tenant,
				"2026-05-01",
				"INV-202605-A101"
		));
		Invoice otherInvoice = invoiceRepository.save(buildInvoice(
				"00000000-0000-0000-0000-000000000402",
				otherUnit,
				otherTenant,
				"2026-05-01",
				"INV-202605-B101"
		));
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
		TenantUnitCredit credit = tenantUnitCreditRepository.save(new TenantUnitCredit(
				UUID.fromString("00000000-0000-0000-0000-000000000701"),
				tenant,
				unit,
				"0.00"
		));
		creditApplicationRepository.save(new CreditApplication(
				UUID.fromString("00000000-0000-0000-0000-000000000801"),
				credit,
				invoice,
				"200000.00",
				LocalDate.parse("2026-05-20")
		));
	}

	@Test
	void cashFlowReturnsIncomeExpenseAndNetSavingWithoutDuplicatingSums() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/reports/cash-flow")
						.header("Authorization", "Bearer " + accessToken)
						.param("property_id", property.getId().toString())
						.param("month", "2026-05"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.property_id").value(property.getId().toString()))
				.andExpect(jsonPath("$.month").value("2026-05"))
				.andExpect(jsonPath("$.total_income").value(1500000.00))
				.andExpect(jsonPath("$.total_expense").value(400000.00))
				.andExpect(jsonPath("$.net_saving").value(1100000.00));
	}

	@Test
	void cashFlowReturnsZeroWhenNoIncomeOrExpensesExist() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/reports/cash-flow")
						.header("Authorization", "Bearer " + accessToken)
						.param("property_id", property.getId().toString())
						.param("month", "2026-07"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total_income").value(0))
				.andExpect(jsonPath("$.total_expense").value(0))
				.andExpect(jsonPath("$.net_saving").value(0));
	}

	@Test
	void cashFlowHandlesPropertyNotFound() throws Exception {
		String accessToken = jwtTokenService.createAccessToken(user);

		mockMvc.perform(get("/api/v1/reports/cash-flow")
						.header("Authorization", "Bearer " + accessToken)
						.param("property_id", "00000000-0000-0000-0000-000000000999")
						.param("month", "2026-05"))
				.andExpect(status().isNotFound());
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
}
