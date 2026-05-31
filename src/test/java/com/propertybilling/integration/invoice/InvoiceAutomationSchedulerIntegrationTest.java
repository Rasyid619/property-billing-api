package com.propertybilling.integration.invoice;

import static org.assertj.core.api.Assertions.assertThat;

import com.propertybilling.entity.Invoice;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.TenantAssignment;
import com.propertybilling.entity.Unit;
import com.propertybilling.integration.AbstractIntegrationTest;
import com.propertybilling.repository.InvoiceRepository;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.TenantAssignmentRepository;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.UnitRepository;
import com.propertybilling.service.InvoiceAutomationScheduler;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
		"app.invoice.automation.enabled=true",
		"app.invoice.automation.cron=0 0 0 25 * *",
		"app.invoice.automation.zone=UTC"
})
/*
 * Integration tests for automated invoice generation through the scheduler.
 */
class InvoiceAutomationSchedulerIntegrationTest extends AbstractIntegrationTest {

	private final InvoiceAutomationScheduler scheduler;
	private final InvoiceRepository invoiceRepository;
	private final PropertyRepository propertyRepository;
	private final TenantAssignmentRepository tenantAssignmentRepository;
	private final TenantRepository tenantRepository;
	private final UnitRepository unitRepository;

	@Autowired
	InvoiceAutomationSchedulerIntegrationTest(
			InvoiceAutomationScheduler scheduler,
			InvoiceRepository invoiceRepository,
			PropertyRepository propertyRepository,
			TenantAssignmentRepository tenantAssignmentRepository,
			TenantRepository tenantRepository,
			UnitRepository unitRepository
	) {
		this.scheduler = scheduler;
		this.invoiceRepository = invoiceRepository;
		this.propertyRepository = propertyRepository;
		this.tenantAssignmentRepository = tenantAssignmentRepository;
		this.tenantRepository = tenantRepository;
		this.unitRepository = unitRepository;
	}

	@BeforeEach
	void setUp() {
		Property activeProperty = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000101",
				"Green Residence",
				true
		));
		Property inactiveProperty = propertyRepository.save(buildProperty(
				"00000000-0000-0000-0000-000000000102",
				"Inactive Residence",
				false
		));
		Unit activeUnit = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000201",
				activeProperty,
				"A-101",
				"750000.00",
				5,
				true
		));
		Unit inactivePropertyUnit = unitRepository.save(buildUnit(
				"00000000-0000-0000-0000-000000000202",
				inactiveProperty,
				"B-101",
				"650000.00",
				7,
				true
		));
		Tenant activeTenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000301",
				"Budi"
		));
		Tenant inactivePropertyTenant = tenantRepository.save(buildTenant(
				"00000000-0000-0000-0000-000000000302",
				"Andi"
		));
		tenantAssignmentRepository.save(buildAssignment(
				"00000000-0000-0000-0000-000000000401",
				activeUnit,
				activeTenant
		));
		tenantAssignmentRepository.save(buildAssignment(
				"00000000-0000-0000-0000-000000000402",
				inactivePropertyUnit,
				inactivePropertyTenant
		));
	}

	@Test
	void schedulerGeneratesNextMonthInvoicesForActivePropertiesOnlyAndSkipsDuplicates() {
		scheduler.generateNextBillingMonthInvoices();
		scheduler.generateNextBillingMonthInvoices();

		List<Invoice> invoices = invoiceRepository.findAll();

		assertThat(invoices).hasSize(1);
		assertThat(invoices.getFirst().getBillingMonth()).isEqualTo(LocalDate.parse("2026-06-01"));
		assertThat(invoices.getFirst().getDueDate()).isEqualTo(LocalDate.parse("2026-06-05"));
		assertThat(invoices.getFirst().getAmount()).isEqualTo("750000.00");
		assertThat(invoices.getFirst().getStatus()).isEqualTo("unpaid");
		assertThat(invoices.getFirst().getUnit().getId())
				.isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000201"));
	}

	private Property buildProperty(String id, String name, boolean active) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Property(
				UUID.fromString(id),
				name,
				null,
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

	private TenantAssignment buildAssignment(String id, Unit unit, Tenant tenant) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new TenantAssignment(
				UUID.fromString(id),
				unit,
				tenant,
				LocalDate.parse("2026-01-01"),
				null,
				true,
				timestamp,
				timestamp
		);
	}

	@TestConfiguration
	static class FixedClockConfig {

		@Bean
		@Primary
		Clock fixedClock() {
			return Clock.fixed(Instant.parse("2026-05-25T00:00:00Z"), ZoneOffset.UTC);
		}
	}
}
