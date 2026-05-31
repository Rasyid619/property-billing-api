package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.propertybilling.constant.InvoiceStatus;
import com.propertybilling.constant.PaymentMethod;
import com.propertybilling.dto.invoice.InvoiceGenerateMonthlyRequest;
import com.propertybilling.dto.invoice.InvoiceIndexResponse;
import com.propertybilling.dto.invoice.InvoiceShowResponse;
import com.propertybilling.dto.invoice.queryresult.InvoiceGenerationTargetQueryResult;
import com.propertybilling.dto.invoice.queryresult.InvoiceIndexQueryResult;
import com.propertybilling.dto.invoice.queryresult.InvoiceShowQueryResult;
import com.propertybilling.dto.payment.PaymentCreateRequest;
import com.propertybilling.dto.payment.PaymentIndexResponse;
import com.propertybilling.dto.payment.queryresult.PaymentIndexQueryResult;
import com.propertybilling.entity.CreditApplication;
import com.propertybilling.entity.Invoice;
import com.propertybilling.entity.Payment;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.TenantUnitCredit;
import com.propertybilling.entity.Unit;
import com.propertybilling.exception.InvoiceGenerationConflictException;
import com.propertybilling.exception.InvoiceNotFoundException;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.CreditApplicationRepository;
import com.propertybilling.repository.InvoiceQueryRepository;
import com.propertybilling.repository.InvoiceRepository;
import com.propertybilling.repository.PaymentRepository;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.TenantUnitCreditRepository;
import com.propertybilling.repository.UnitRepository;
import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
	private final PaymentRepository paymentRepository = Mockito.mock(PaymentRepository.class);
	private final CreditApplicationRepository creditApplicationRepository = Mockito.mock(CreditApplicationRepository.class);
	private final PropertyRepository propertyRepository = Mockito.mock(PropertyRepository.class);
	private final TenantRepository tenantRepository = Mockito.mock(TenantRepository.class);
	private final TenantUnitCreditRepository tenantUnitCreditRepository = Mockito.mock(TenantUnitCreditRepository.class);
	private final UnitRepository unitRepository = Mockito.mock(UnitRepository.class);
	private final Clock clock = Clock.fixed(Instant.parse("2026-05-31T00:00:00Z"), ZoneOffset.UTC);
	private final InvoiceService invoiceService = new InvoiceService(
			invoiceQueryRepository,
			invoiceRepository,
			paymentRepository,
			creditApplicationRepository,
			propertyRepository,
			tenantRepository,
			tenantUnitCreditRepository,
			unitRepository,
			clock
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
			when(tenantUnitCreditRepository.findByTenantIdAndUnitIdForUpdate(tenantId, unitId))
					.thenReturn(Optional.empty());

			invoiceService.generateMonthlyInvoices(new InvoiceGenerateMonthlyRequest(
					propertyId,
					LocalDate.parse("2026-05-01")
			));

			verify(propertyRepository, times(1)).findByIdForUpdate(propertyId);
			verify(unitRepository, times(1)).findMonthlyInvoiceGenerationTargets(propertyId);
			verify(invoiceRepository, times(1))
					.existsByUnitIdsAndBillingMonth(List.of(unitId), LocalDate.parse("2026-05-01"));
			verify(invoiceRepository, times(1)).saveAll(invoicesCaptor.capture());
			verify(tenantUnitCreditRepository, times(1)).findByTenantIdAndUnitIdForUpdate(tenantId, unitId);
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
		assertThat(response.invoices().getFirst().paidAmount()).isZero();
		assertThat(response.invoices().getFirst().creditAppliedAmount()).isZero();
		assertThat(response.invoices().getFirst().amountDue()).isEqualByComparingTo("750000.00");
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

	@Nested
	/*
	 * Service tests for listing payments recorded for one invoice.
	 */
	class ListInvoicePayments {

		@Test
		void returnsInvoicePayments() {
			UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			UUID paymentId = UUID.fromString("00000000-0000-0000-0000-000000000501");
			when(invoiceRepository.existsById(invoiceId)).thenReturn(true);
			when(paymentRepository.findIndexByInvoiceId(invoiceId)).thenReturn(List.of(new PaymentIndexQueryResult(
					paymentId,
					invoiceId,
					"750000.00",
					LocalDate.parse("2026-05-08"),
					"bank_transfer",
					"BCA-123456",
					"Paid by tenant",
					"paid"
			)));

			PaymentIndexResponse response = invoiceService.listPayments(invoiceId);

			assertThat(response.count()).isEqualTo(1);
			assertThat(response.payments()).hasSize(1);
			assertThat(response.payments().getFirst().id()).isEqualTo(paymentId);
			assertThat(response.payments().getFirst().invoiceId()).isEqualTo(invoiceId);
			assertThat(response.payments().getFirst().amount()).isEqualByComparingTo("750000.00");
			assertThat(response.payments().getFirst().paymentDate()).isEqualTo(LocalDate.parse("2026-05-08"));
			assertThat(response.payments().getFirst().paymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
			assertThat(response.payments().getFirst().referenceNumber()).isEqualTo("BCA-123456");
			assertThat(response.payments().getFirst().note()).isEqualTo("Paid by tenant");
			assertThat(response.payments().getFirst().invoiceStatus()).isEqualTo(InvoiceStatus.PAID);
			verify(invoiceRepository, times(1)).existsById(invoiceId);
			verify(paymentRepository, times(1)).findIndexByInvoiceId(invoiceId);
		}

		@Test
		void returnsEmptyPaymentsWhenInvoiceHasNoPayments() {
			UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			when(invoiceRepository.existsById(invoiceId)).thenReturn(true);
			when(paymentRepository.findIndexByInvoiceId(invoiceId)).thenReturn(List.of());

			PaymentIndexResponse response = invoiceService.listPayments(invoiceId);

			assertThat(response.count()).isZero();
			assertThat(response.payments()).isEmpty();
			verify(invoiceRepository, times(1)).existsById(invoiceId);
			verify(paymentRepository, times(1)).findIndexByInvoiceId(invoiceId);
		}

		@Test
		void throwsNotFoundWhenInvoiceDoesNotExist() {
			UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(invoiceRepository.existsById(invoiceId)).thenReturn(false);

			assertThatThrownBy(() -> invoiceService.listPayments(invoiceId))
					.isInstanceOf(InvoiceNotFoundException.class);

			verify(invoiceRepository, times(1)).existsById(invoiceId);
			verify(paymentRepository, never()).findIndexByInvoiceId(Mockito.any());
		}
	}

	@Nested
	/*
	 * Service tests for showing one invoice.
	 */
	class ShowInvoice {

		@Test
		void returnsInvoice() {
			UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			when(invoiceRepository.findShow(invoiceId)).thenReturn(Optional.of(new InvoiceShowQueryResult(
					invoiceId,
					unitId,
					tenantId,
					LocalDate.parse("2026-05-01"),
					"INV-202605-A101",
					"750000.00",
					LocalDate.parse("2026-05-10"),
					"unpaid"
			)));

			InvoiceShowResponse response = invoiceService.getInvoice(invoiceId);

			assertThat(response.id()).isEqualTo(invoiceId);
			assertThat(response.unitId()).isEqualTo(unitId);
			assertThat(response.tenantId()).isEqualTo(tenantId);
			assertThat(response.billingMonth()).isEqualTo(LocalDate.parse("2026-05-01"));
			assertThat(response.invoiceNumber()).isEqualTo("INV-202605-A101");
			assertThat(response.amount()).isEqualByComparingTo("750000.00");
			assertThat(response.paidAmount()).isZero();
			assertThat(response.creditAppliedAmount()).isZero();
			assertThat(response.amountDue()).isEqualByComparingTo("750000.00");
			assertThat(response.dueDate()).isEqualTo(LocalDate.parse("2026-05-10"));
			assertThat(response.status()).isEqualTo("unpaid");
			verify(invoiceRepository, times(1)).findShow(invoiceId);
		}

		@Test
		void throwsNotFoundWhenInvoiceDoesNotExist() {
			UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(invoiceRepository.findShow(invoiceId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> invoiceService.getInvoice(invoiceId))
					.isInstanceOf(InvoiceNotFoundException.class);

			verify(invoiceRepository, times(1)).findShow(invoiceId);
		}
	}

	@Nested
	/*
	 * Service tests for recording invoice payments.
	 */
	class RecordPayment {

		@Test
		void recordsFullPaymentAndMarksInvoicePaid() {
			UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			Invoice invoice = buildInvoice(invoiceId, "750000.00", LocalDate.parse("2026-06-10"), "unpaid");
			ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
			when(invoiceRepository.findPaymentAllocationInvoicesForUpdate(
					invoiceId,
					openInvoiceStatuses()
			)).thenReturn(List.of(invoice));
			when(paymentRepository.sumAmountByInvoiceId(invoiceId))
					.thenReturn(new java.math.BigDecimal("0.00"))
					.thenReturn(new java.math.BigDecimal("750000.00"));

			invoiceService.recordPayment(invoiceId, buildPaymentRequest("750000.00"));

			verify(paymentRepository, times(1)).save(paymentCaptor.capture());
			assertThat(paymentCaptor.getValue().getInvoice()).isEqualTo(invoice);
			assertThat(paymentCaptor.getValue().getAmount()).isEqualTo("750000.00");
			assertThat(paymentCaptor.getValue().getPaymentDate()).isEqualTo(LocalDate.parse("2026-05-08"));
			assertThat(paymentCaptor.getValue().getPaymentMethod()).isEqualTo("bank_transfer");
			assertThat(paymentCaptor.getValue().getReferenceNumber()).isEqualTo("BCA-123456");
			assertThat(paymentCaptor.getValue().getNote()).isEqualTo("Paid by tenant");
			assertThat(invoice.getStatus()).isEqualTo("paid");
			verify(invoiceRepository, times(1)).save(invoice);
			verify(invoiceRepository, times(1)).findPaymentAllocationInvoicesForUpdate(
					invoiceId,
					openInvoiceStatuses()
			);
		}

		@Test
		void recordsPartialPaymentAndMarksInvoicePartialWhenDueDateIsNotPast() {
			UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			Invoice invoice = buildInvoice(invoiceId, "750000.00", LocalDate.parse("2026-06-10"), "unpaid");
			when(invoiceRepository.findPaymentAllocationInvoicesForUpdate(
					invoiceId,
					openInvoiceStatuses()
			)).thenReturn(List.of(invoice));
			when(paymentRepository.sumAmountByInvoiceId(invoiceId))
					.thenReturn(new java.math.BigDecimal("0.00"))
					.thenReturn(new java.math.BigDecimal("300000.00"));

			invoiceService.recordPayment(invoiceId, buildPaymentRequest("300000.00"));

			assertThat(invoice.getStatus()).isEqualTo("partial");
			verify(paymentRepository, times(1)).save(Mockito.any(Payment.class));
			verify(invoiceRepository, times(1)).save(invoice);
		}

		@Test
		void marksPastDueInvoiceOverdueWhenPaymentDoesNotCompleteIt() {
			UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			Invoice invoice = buildInvoice(invoiceId, "750000.00", LocalDate.parse("2026-05-10"), "unpaid");
			when(invoiceRepository.findPaymentAllocationInvoicesForUpdate(
					invoiceId,
					openInvoiceStatuses()
			)).thenReturn(List.of(invoice));
			when(paymentRepository.sumAmountByInvoiceId(invoiceId))
					.thenReturn(new java.math.BigDecimal("0.00"))
					.thenReturn(new java.math.BigDecimal("300000.00"));

			invoiceService.recordPayment(invoiceId, buildPaymentRequest("300000.00"));

			assertThat(invoice.getStatus()).isEqualTo("overdue");
			verify(paymentRepository, times(1)).save(Mockito.any(Payment.class));
			verify(invoiceRepository, times(1)).save(invoice);
		}

		@Test
		void recordsMultiplePaymentsAndMarksInvoicePaidWhenTotalReachesInvoiceAmount() {
			UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			Invoice invoice = buildInvoice(invoiceId, "750000.00", LocalDate.parse("2026-06-10"), "partial");
			when(invoiceRepository.findPaymentAllocationInvoicesForUpdate(
					invoiceId,
					openInvoiceStatuses()
			)).thenReturn(List.of(invoice));
			when(paymentRepository.sumAmountByInvoiceId(invoiceId))
					.thenReturn(new java.math.BigDecimal("300000.00"))
					.thenReturn(new java.math.BigDecimal("750000.00"));

			invoiceService.recordPayment(invoiceId, buildPaymentRequest("450000.00"));

			assertThat(invoice.getStatus()).isEqualTo("paid");
			verify(paymentRepository, times(1)).save(Mockito.argThat(payment ->
					"450000.00".equals(payment.getAmount())
			));
			verify(invoiceRepository, times(1)).save(invoice);
		}

		@Test
		void allocatesOverpaymentToOldestOpenInvoiceForSameTenant() {
			UUID selectedInvoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			UUID nextInvoiceId = UUID.fromString("00000000-0000-0000-0000-000000000402");
			Invoice selectedInvoice = buildInvoice(selectedInvoiceId, "750000.00", LocalDate.parse("2026-05-10"), "unpaid");
			Invoice nextInvoice = buildInvoice(nextInvoiceId, "750000.00", LocalDate.parse("2026-06-10"), "unpaid");
			ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
			ArgumentCaptor<CreditApplication> creditApplicationCaptor = ArgumentCaptor.forClass(CreditApplication.class);
			when(invoiceRepository.findPaymentAllocationInvoicesForUpdate(
					selectedInvoiceId,
					openInvoiceStatuses()
			)).thenReturn(List.of(selectedInvoice, nextInvoice));
			when(paymentRepository.sumAmountByInvoiceId(selectedInvoiceId))
					.thenReturn(new java.math.BigDecimal("0.00"));
			when(paymentRepository.sumAmountByInvoiceId(nextInvoiceId)).thenReturn(new java.math.BigDecimal("0.00"));
			when(creditApplicationRepository.sumAmountByInvoiceId(selectedInvoiceId))
					.thenReturn(new java.math.BigDecimal("0.00"));
			when(creditApplicationRepository.sumAmountByInvoiceId(nextInvoiceId)).thenReturn(new java.math.BigDecimal("0.00"));
			when(tenantUnitCreditRepository.findByTenantIdAndUnitIdForUpdate(
					selectedInvoice.getTenant().getId(),
					selectedInvoice.getUnit().getId()
			)).thenReturn(Optional.empty());

			invoiceService.recordPayment(selectedInvoiceId, buildPaymentRequest("1000000.00"));

			verify(paymentRepository, times(1)).save(paymentCaptor.capture());
			assertThat(paymentCaptor.getValue().getAmount()).isEqualTo("1000000.00");
			assertThat(paymentCaptor.getValue().getInvoice()).isEqualTo(selectedInvoice);
			verify(creditApplicationRepository, times(1)).save(creditApplicationCaptor.capture());
			assertThat(creditApplicationCaptor.getValue().getInvoice()).isEqualTo(nextInvoice);
			assertThat(creditApplicationCaptor.getValue().getAmount()).isEqualTo("250000.00");
			assertThat(creditApplicationCaptor.getValue().getAppliedDate()).isEqualTo(LocalDate.parse("2026-05-08"));
			assertThat(selectedInvoice.getStatus()).isEqualTo("paid");
			assertThat(nextInvoice.getStatus()).isEqualTo("partial");
			verify(invoiceRepository, times(1)).save(selectedInvoice);
			verify(invoiceRepository, times(1)).save(nextInvoice);
		}

		@Test
		void preservesRemainingSurplusOnSelectedInvoiceWhenNoOpenInvoicesCanReceiveIt() {
			UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			Invoice invoice = buildInvoice(invoiceId, "750000.00", LocalDate.parse("2026-05-10"), "unpaid");
			ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
			ArgumentCaptor<TenantUnitCredit> creditCaptor = ArgumentCaptor.forClass(TenantUnitCredit.class);
			when(invoiceRepository.findPaymentAllocationInvoicesForUpdate(
					invoiceId,
					openInvoiceStatuses()
			)).thenReturn(List.of(invoice));
			when(paymentRepository.sumAmountByInvoiceId(invoiceId))
					.thenReturn(new java.math.BigDecimal("0.00"));
			when(creditApplicationRepository.sumAmountByInvoiceId(invoiceId))
					.thenReturn(new java.math.BigDecimal("0.00"));
			when(tenantUnitCreditRepository.findByTenantIdAndUnitIdForUpdate(
					invoice.getTenant().getId(),
					invoice.getUnit().getId()
			)).thenReturn(Optional.empty());

			invoiceService.recordPayment(invoiceId, buildPaymentRequest("900000.00"));

			verify(paymentRepository, times(1)).save(paymentCaptor.capture());
			assertThat(paymentCaptor.getValue().getAmount()).isEqualTo("900000.00");
			assertThat(paymentCaptor.getValue().getInvoice()).isEqualTo(invoice);
			verify(tenantUnitCreditRepository, times(2)).save(creditCaptor.capture());
			assertThat(creditCaptor.getAllValues().getLast().getBalance()).isEqualTo("150000.00");
			verify(creditApplicationRepository, never()).save(Mockito.any(CreditApplication.class));
			assertThat(invoice.getStatus()).isEqualTo("paid");
			verify(invoiceRepository, times(1)).save(invoice);
		}

		@Test
		void throwsNotFoundWhenInvoiceDoesNotExist() {
			UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(invoiceRepository.findPaymentAllocationInvoicesForUpdate(
					invoiceId,
					openInvoiceStatuses()
			)).thenReturn(List.of());

			assertThatThrownBy(() -> invoiceService.recordPayment(invoiceId, buildPaymentRequest("750000.00")))
					.isInstanceOf(InvoiceNotFoundException.class);

			verify(invoiceRepository, times(1)).findPaymentAllocationInvoicesForUpdate(
					invoiceId,
					openInvoiceStatuses()
			);
			verifyNoInteractions(paymentRepository);
		}
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

	private Invoice buildInvoice(
			UUID invoiceId,
			String amount,
			LocalDate dueDate,
			String status
	) {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
		UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
		return new Invoice(
				invoiceId,
				buildUnit(unitId, propertyId, "A-101", amount, dueDate.getDayOfMonth(), true),
				buildTenant(tenantId, "Budi"),
				dueDate.withDayOfMonth(1),
				"INV-" + invoiceId,
				amount,
				dueDate,
				status
		);
	}

	private PaymentCreateRequest buildPaymentRequest(String amount) {
		return new PaymentCreateRequest(
				new java.math.BigDecimal(amount),
				LocalDate.parse("2026-05-08"),
				PaymentMethod.BANK_TRANSFER,
				"BCA-123456",
				"Paid by tenant"
		);
	}

	private List<String> openInvoiceStatuses() {
		return List.of(
				InvoiceStatus.UNPAID.value(),
				InvoiceStatus.PARTIAL.value(),
				InvoiceStatus.OVERDUE.value()
		);
	}

	private List<Invoice> toList(Iterable<?> invoices) {
		return StreamSupport.stream(invoices.spliterator(), false)
				.map(Invoice.class::cast)
				.toList();
	}
}
