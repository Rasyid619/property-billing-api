package com.propertybilling.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.propertybilling.dto.invoice.InvoiceGenerateMonthlyRequest;
import com.propertybilling.exception.InvoiceGenerationConflictException;
import com.propertybilling.repository.PropertyRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/*
 * Unit tests for automated monthly invoice generation scheduling.
 */
class InvoiceAutomationSchedulerTest {

	private final PropertyRepository propertyRepository = Mockito.mock(PropertyRepository.class);
	private final InvoiceService invoiceService = Mockito.mock(InvoiceService.class);
	private final Clock clock = Clock.fixed(Instant.parse("2026-05-25T00:00:00Z"), ZoneOffset.UTC);
	private final InvoiceAutomationScheduler scheduler = new InvoiceAutomationScheduler(
			propertyRepository,
			invoiceService,
			clock
	);

	@Test
	void generatesNextBillingMonthInvoicesForActiveProperties() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		when(propertyRepository.findActiveIdsForInvoiceAutomation()).thenReturn(List.of(propertyId));

		scheduler.generateNextBillingMonthInvoices();

		verify(propertyRepository, times(1)).findActiveIdsForInvoiceAutomation();
		verify(invoiceService, times(1)).generateMonthlyInvoices(Mockito.argThat(request ->
				propertyId.equals(request.propertyId())
						&& LocalDate.parse("2026-06-01").equals(request.billingMonth())
		));
	}

	@Test
	void continuesWhenOnePropertyAlreadyHasInvoicesForNextMonth() {
		UUID conflictingPropertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		UUID nextPropertyId = UUID.fromString("00000000-0000-0000-0000-000000000102");
		when(propertyRepository.findActiveIdsForInvoiceAutomation()).thenReturn(List.of(
				conflictingPropertyId,
				nextPropertyId
		));
		Mockito.doThrow(new InvoiceGenerationConflictException())
				.when(invoiceService)
				.generateMonthlyInvoices(Mockito.argThat(request -> conflictingPropertyId.equals(request.propertyId())));

		scheduler.generateNextBillingMonthInvoices();

		verify(invoiceService, times(1)).generateMonthlyInvoices(new InvoiceGenerateMonthlyRequest(
				conflictingPropertyId,
				LocalDate.parse("2026-06-01")
		));
		verify(invoiceService, times(1)).generateMonthlyInvoices(new InvoiceGenerateMonthlyRequest(
				nextPropertyId,
				LocalDate.parse("2026-06-01")
		));
	}
}
