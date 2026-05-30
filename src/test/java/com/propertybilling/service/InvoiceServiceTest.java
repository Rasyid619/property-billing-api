package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.propertybilling.dto.invoice.InvoiceGenerateMonthlyRequest;
import com.propertybilling.dto.invoice.InvoiceIndexResponse;
import com.propertybilling.dto.invoice.queryresult.InvoiceGenerationTargetQueryResult;
import com.propertybilling.dto.invoice.queryresult.InvoiceIndexQueryResult;
import com.propertybilling.entity.Invoice;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.Unit;
import com.propertybilling.exception.InvoiceGenerationConflictException;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.InvoiceQueryRepository;
import com.propertybilling.repository.InvoiceRepository;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.UnitRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

/*
 * Unit tests for invoice retrieval workflows.
 */
class InvoiceServiceTest {

	private final InvoiceQueryRepository invoiceQueryRepository = Mockito.mock(InvoiceQueryRepository.class);
	private final InvoiceRepository invoiceRepository = Mockito.mock(InvoiceRepository.class);
	private final PropertyRepository propertyRepository = Mockito.mock(PropertyRepository.class);
	private final TenantRepository tenantRepository = Mockito.mock(TenantRepository.class);
	private final UnitRepository unitRepository = Mockito.mock(UnitRepository.class);
	private final InvoiceService invoiceService = new InvoiceService(
			invoiceQueryRepository,
			invoiceRepository,
			propertyRepository,
			tenantRepository,
			unitRepository
	);

	@Nested
	/*
	 * Service tests for generating monthly invoices.
	 */
	class GenerateMonthlyInvoices {

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void generatesInvoicesForActiveUnitsWithActiveTenants() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			Unit unit = buildUnit(unitId, propertyId, "A-101", "750000.00", 10, true);
			Tenant tenant = buildTenant(tenantId, "Budi");
			ArgumentCaptor<Iterable<Invoice>> invoicesCaptor = ArgumentCaptor.forClass(Iterable.class);
			when(propertyRepository.findByIdForUpdate(propertyId)).thenReturn(Optional.of(buildProperty(propertyId, true)));
			when(unitRepository.findMonthlyInvoiceGenerationTargets(propertyId)).thenReturn(List.of(
					new InvoiceGenerationTargetQueryResult(unitId, tenantId, "A-101", "750000.00", 10)
			));
			when(invoiceRepository.existsByUnitIdsAndBillingMonth(
					List.of(unitId),
					LocalDate.parse("2026-05-01")
			)).thenReturn(false);
			when(unitRepository.getReferenceById(unitId)).thenReturn(unit);
			when(tenantRepository.getReferenceById(tenantId)).thenReturn(tenant);

			invoiceService.generateMonthlyInvoices(new InvoiceGenerateMonthlyRequest(
					propertyId,
					LocalDate.parse("2026-05-01")
			));

			verify(propertyRepository, times(1)).findByIdForUpdate(propertyId);
			verify(unitRepository, times(1)).findMonthlyInvoiceGenerationTargets(propertyId);
			verify(invoiceRepository, times(1))
					.existsByUnitIdsAndBillingMonth(List.of(unitId), LocalDate.parse("2026-05-01"));
			verify(invoiceRepository, times(1)).saveAll(invoicesCaptor.capture());
			List<Invoice> invoices = toList(invoicesCaptor.getValue());
			assertThat(invoices).hasSize(1);
			assertThat(invoices.getFirst().getId()).isNotNull();
			assertThat(invoices.getFirst().getUnit()).isEqualTo(unit);
			assertThat(invoices.getFirst().getTenant()).isEqualTo(tenant);
			assertThat(invoices.getFirst().getBillingMonth()).isEqualTo(LocalDate.parse("2026-05-01"));
			assertThat(invoices.getFirst().getInvoiceNumber())
					.isEqualTo("INV-202605-00000000000000000000000000000201");
			assertThat(invoices.getFirst().getAmount()).isEqualTo("750000.00");
			assertThat(invoices.getFirst().getDueDate()).isEqualTo(LocalDate.parse("2026-05-10"));
			assertThat(invoices.getFirst().getStatus()).isEqualTo("unpaid");
			assertThat(invoices.getFirst().getCreatedAt()).isNull();
			assertThat(invoices.getFirst().getUpdatedAt()).isNull();
		}

		@Test
		void skipsUnitsWithoutActiveTenants() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyRepository.findByIdForUpdate(propertyId)).thenReturn(Optional.of(buildProperty(propertyId, true)));
			when(unitRepository.findMonthlyInvoiceGenerationTargets(propertyId)).thenReturn(List.of());

			invoiceService.generateMonthlyInvoices(new InvoiceGenerateMonthlyRequest(
					propertyId,
					LocalDate.parse("2026-05-01")
			));

			verify(propertyRepository, times(1)).findByIdForUpdate(propertyId);
			verify(unitRepository, times(1)).findMonthlyInvoiceGenerationTargets(propertyId);
			verify(invoiceRepository, never()).existsByUnitIdsAndBillingMonth(Mockito.any(), Mockito.any());
			verify(invoiceRepository, never()).saveAll(Mockito.any());
		}

		@Test
		void throwsNotFoundWhenPropertyDoesNotExist() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(propertyRepository.findByIdForUpdate(propertyId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> invoiceService.generateMonthlyInvoices(new InvoiceGenerateMonthlyRequest(
					propertyId,
					LocalDate.parse("2026-05-01")
			))).isInstanceOf(PropertyNotFoundException.class);

			verify(propertyRepository, times(1)).findByIdForUpdate(propertyId);
			verifyNoInteractions(unitRepository, invoiceRepository, tenantRepository);
		}

		@Test
		void throwsConflictWhenPropertyIsInactive() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyRepository.findByIdForUpdate(propertyId)).thenReturn(Optional.of(buildProperty(propertyId, false)));

			assertThatThrownBy(() -> invoiceService.generateMonthlyInvoices(new InvoiceGenerateMonthlyRequest(
					propertyId,
					LocalDate.parse("2026-05-01")
			))).isInstanceOf(InvoiceGenerationConflictException.class);

			verify(propertyRepository, times(1)).findByIdForUpdate(propertyId);
			verifyNoInteractions(unitRepository, invoiceRepository, tenantRepository);
		}

		@Test
		void throwsConflictWhenAnyEligibleUnitAlreadyHasInvoiceForMonth() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			when(propertyRepository.findByIdForUpdate(propertyId)).thenReturn(Optional.of(buildProperty(propertyId, true)));
			when(unitRepository.findMonthlyInvoiceGenerationTargets(propertyId)).thenReturn(List.of(
					new InvoiceGenerationTargetQueryResult(unitId, tenantId, "A-101", "750000.00", 10)
			));
			when(invoiceRepository.existsByUnitIdsAndBillingMonth(
					List.of(unitId),
					LocalDate.parse("2026-05-01")
			)).thenReturn(true);

			assertThatThrownBy(() -> invoiceService.generateMonthlyInvoices(new InvoiceGenerateMonthlyRequest(
					propertyId,
					LocalDate.parse("2026-05-01")
			))).isInstanceOf(InvoiceGenerationConflictException.class);

			verify(invoiceRepository, times(1))
					.existsByUnitIdsAndBillingMonth(List.of(unitId), LocalDate.parse("2026-05-01"));
			verify(invoiceRepository, never()).saveAll(Mockito.any());
		}

		@Test
		void throwsConflictWhenDatabaseRejectsDuplicateInvoice() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			when(propertyRepository.findByIdForUpdate(propertyId)).thenReturn(Optional.of(buildProperty(propertyId, true)));
			when(unitRepository.findMonthlyInvoiceGenerationTargets(propertyId)).thenReturn(List.of(
					new InvoiceGenerationTargetQueryResult(unitId, tenantId, "A-101", "750000.00", 10)
			));
			when(invoiceRepository.existsByUnitIdsAndBillingMonth(
					List.of(unitId),
					LocalDate.parse("2026-05-01")
			)).thenReturn(false);
			when(unitRepository.getReferenceById(unitId)).thenReturn(buildUnit(
					unitId,
					propertyId,
					"A-101",
					"750000.00",
					10,
					true
			));
			when(tenantRepository.getReferenceById(tenantId)).thenReturn(buildTenant(tenantId, "Budi"));
			Mockito.doThrow(new DataIntegrityViolationException("duplicate invoice"))
					.when(invoiceRepository)
					.saveAll(Mockito.any());

			assertThatThrownBy(() -> invoiceService.generateMonthlyInvoices(new InvoiceGenerateMonthlyRequest(
					propertyId,
					LocalDate.parse("2026-05-01")
			))).isInstanceOf(InvoiceGenerationConflictException.class);

			verify(invoiceRepository, times(1)).saveAll(Mockito.any());
		}
	}

	@Test
	void listsInvoicesWithFilters() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
		UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
		when(invoiceQueryRepository.findIndex(
				Mockito.eq(propertyId),
				Mockito.eq(unitId),
				Mockito.eq(tenantId),
				Mockito.eq(LocalDate.parse("2026-05-01")),
				Mockito.eq("unpaid"),
				Mockito.any(Pageable.class)
		)).thenReturn(List.of(new InvoiceIndexQueryResult(
				UUID.fromString("00000000-0000-0000-0000-000000000401"),
				unitId,
				tenantId,
				LocalDate.parse("2026-05-01"),
				"INV-202605-A101",
				"750000.00",
				LocalDate.parse("2026-05-10"),
				"unpaid"
		)));

		InvoiceIndexResponse response = invoiceService.listInvoices(
				propertyId,
				unitId,
				tenantId,
				"2026-05",
				"unpaid",
				0,
				100
		);

		assertThat(response.count()).isEqualTo(1);
		assertThat(response.invoices()).hasSize(1);
		assertThat(response.invoices().getFirst().id())
				.isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000401"));
		assertThat(response.invoices().getFirst().unitId()).isEqualTo(unitId);
		assertThat(response.invoices().getFirst().tenantId()).isEqualTo(tenantId);
		assertThat(response.invoices().getFirst().billingMonth()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(response.invoices().getFirst().invoiceNumber()).isEqualTo("INV-202605-A101");
		assertThat(response.invoices().getFirst().amount()).isEqualByComparingTo("750000.00");
		assertThat(response.invoices().getFirst().dueDate()).isEqualTo(LocalDate.parse("2026-05-10"));
		assertThat(response.invoices().getFirst().status()).isEqualTo("unpaid");
		verify(invoiceQueryRepository, times(1)).findIndex(
				Mockito.eq(propertyId),
				Mockito.eq(unitId),
				Mockito.eq(tenantId),
				Mockito.eq(LocalDate.parse("2026-05-01")),
				Mockito.eq("unpaid"),
				Mockito.any(Pageable.class)
		);
	}

	@Test
	void passesNullFiltersWhenOptionalFiltersAreUnset() {
		when(invoiceQueryRepository.findIndex(
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.any(Pageable.class)
		)).thenReturn(List.of());

		InvoiceIndexResponse response = invoiceService.listInvoices(null, null, null, null, null, 0, 100);

		assertThat(response.count()).isZero();
		assertThat(response.invoices()).isEmpty();
		verify(invoiceQueryRepository, times(1)).findIndex(
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.any(Pageable.class)
		);
	}

	@Test
	void passesPageRequestToRepository() {
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		when(invoiceQueryRepository.findIndex(
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.any(Pageable.class)
		)).thenReturn(List.of());

		invoiceService.listInvoices(null, null, null, null, null, 200, 100);

		verify(invoiceQueryRepository, times(1)).findIndex(
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				pageableCaptor.capture()
		);
		assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
		assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
	}

	private Property buildProperty(UUID propertyId, boolean active) {
		return new Property(
				propertyId,
				"Green Residence",
				"Bekasi",
				active
		);
	}

	private Unit buildUnit(
			UUID unitId,
			UUID propertyId,
			String unitNumber,
			String monthlyFee,
			int dueDay,
			boolean active
	) {
		return new Unit(
				unitId,
				buildProperty(propertyId, true),
				unitNumber,
				monthlyFee,
				dueDay,
				active
		);
	}

	private Tenant buildTenant(UUID tenantId, String name) {
		return new Tenant(
				tenantId,
				name,
				null,
				null
		);
	}

	private List<Invoice> toList(Iterable<?> invoices) {
		return StreamSupport.stream(invoices.spliterator(), false)
				.map(Invoice.class::cast)
				.toList();
	}
}
