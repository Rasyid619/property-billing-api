package com.propertybilling.service;

import com.propertybilling.dto.invoice.InvoiceIndexElement;
import com.propertybilling.dto.invoice.InvoiceIndexResponse;
import com.propertybilling.dto.invoice.queryresult.InvoiceIndexQueryResult;
import com.propertybilling.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for invoice retrieval.
 */
public class InvoiceService {

	private final InvoiceRepository invoiceRepository;

	/**
	 * Lists invoices using optional filters.
	 *
	 * @param propertyId optional owning property filter
	 * @param unitId optional unit filter
	 * @param tenantId optional tenant filter
	 * @param month optional month in YYYY-MM format
	 * @param status optional invoice status filter
	 * @param offset number of invoices to skip
	 * @param limit maximum number of invoices to return
	 * @return invoice index response
	 */
	public InvoiceIndexResponse listInvoices(
			UUID propertyId,
			UUID unitId,
			UUID tenantId,
			String month,
			String status,
			int offset,
			int limit
	) {
		LocalDate billingMonth = toBillingMonth(month);
		List<InvoiceIndexElement> invoices = invoiceRepository.findIndex(
				propertyId,
				unitId,
				tenantId,
				billingMonth,
				toStatusFilter(status),
				PageRequest.of(offset / limit, limit)
		)
				.stream()
				.map(this::toIndexElement)
				.toList();

		return new InvoiceIndexResponse(invoices.size(), invoices);
	}

	private LocalDate toBillingMonth(String month) {
		if (month == null || month.isBlank()) {
			return null;
		}

		return YearMonth.parse(month).atDay(1);
	}

	private String toStatusFilter(String status) {
		if (status == null || status.isBlank()) {
			return null;
		}

		return status;
	}

	private InvoiceIndexElement toIndexElement(InvoiceIndexQueryResult invoice) {
		return new InvoiceIndexElement(
				invoice.id(),
				invoice.unitId(),
				invoice.tenantId(),
				invoice.billingMonth(),
				invoice.invoiceNumber(),
				new BigDecimal(invoice.amount()),
				invoice.dueDate(),
				invoice.status()
		);
	}
}
