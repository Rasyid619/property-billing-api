package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.propertybilling.dto.invoice.InvoiceIndexResponse;
import com.propertybilling.dto.invoice.queryresult.InvoiceIndexQueryResult;
import com.propertybilling.repository.InvoiceRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;

/*
 * Unit tests for invoice retrieval workflows.
 */
class InvoiceServiceTest {

	private final InvoiceRepository invoiceRepository = Mockito.mock(InvoiceRepository.class);
	private final InvoiceService invoiceService = new InvoiceService(invoiceRepository);

	@Test
	void listsInvoicesWithFilters() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
		UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
		when(invoiceRepository.findIndex(
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
		verify(invoiceRepository, times(1)).findIndex(
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
		when(invoiceRepository.findIndex(
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
		verify(invoiceRepository, times(1)).findIndex(
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
		when(invoiceRepository.findIndex(
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.isNull(),
				Mockito.any(Pageable.class)
		)).thenReturn(List.of());

		invoiceService.listInvoices(null, null, null, null, null, 200, 100);

		verify(invoiceRepository, times(1)).findIndex(
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
}
