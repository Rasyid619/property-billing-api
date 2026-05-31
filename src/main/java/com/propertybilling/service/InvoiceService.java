package com.propertybilling.service;

import com.propertybilling.dto.invoice.InvoiceGenerateMonthlyRequest;
import com.propertybilling.dto.invoice.InvoiceIndexElement;
import com.propertybilling.dto.invoice.InvoiceIndexResponse;
import com.propertybilling.dto.invoice.InvoiceShowResponse;
import com.propertybilling.dto.invoice.queryresult.InvoiceGenerationTargetQueryResult;
import com.propertybilling.dto.invoice.queryresult.InvoiceIndexQueryResult;
import com.propertybilling.dto.invoice.queryresult.InvoiceShowQueryResult;
import com.propertybilling.entity.Invoice;
import com.propertybilling.entity.Property;
import com.propertybilling.exception.InvoiceGenerationConflictException;
import com.propertybilling.exception.InvoiceNotFoundException;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.InvoiceQueryRepository;
import com.propertybilling.repository.InvoiceRepository;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.UnitRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for invoice retrieval and generation.
 */
public class InvoiceService {

	private final InvoiceQueryRepository invoiceQueryRepository;
	private final InvoiceRepository invoiceRepository;
	private final PropertyRepository propertyRepository;
	private final TenantRepository tenantRepository;
	private final UnitRepository unitRepository;

	private static final String INITIAL_INVOICE_STATUS = "unpaid";

	/**
	 * Generates monthly invoices for active units with active tenants.
	 *
	 * @param request generation request
	 * @throws PropertyNotFoundException when no property exists for the request
	 * @throws InvoiceGenerationConflictException when the property is inactive or a duplicate invoice exists
	 */
	@Transactional
	public void generateMonthlyInvoices(InvoiceGenerateMonthlyRequest request) {
		Property property = propertyRepository.findByIdForUpdate(request.propertyId())
				.orElseThrow(PropertyNotFoundException::new);

		if (!property.isActive()) {
			throw new InvoiceGenerationConflictException();
		}

		List<InvoiceGenerationTargetQueryResult> targets =
				unitRepository.findMonthlyInvoiceGenerationTargets(request.propertyId());

		if (targets.isEmpty()) {
			return;
		}

		if (invoiceRepository.existsByUnitIdsAndBillingMonth(toUnitIds(targets), request.billingMonth())) {
			throw new InvoiceGenerationConflictException();
		}

		try {
			invoiceRepository.saveAll(toInvoices(targets, request.billingMonth()));
		} catch (DataIntegrityViolationException exception) {
			throw new InvoiceGenerationConflictException();
		}
	}

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
		List<InvoiceIndexElement> invoices = invoiceQueryRepository.findIndex(
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

	/**
	 * Gets one invoice by ID.
	 *
	 * @param invoiceId invoice identifier
	 * @return invoice detail response
	 * @throws InvoiceNotFoundException when no invoice exists for the ID
	 */
	public InvoiceShowResponse getInvoice(UUID invoiceId) {
		return invoiceRepository.findShow(invoiceId)
				.map(this::toShowResponse)
				.orElseThrow(InvoiceNotFoundException::new);
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

	private List<UUID> toUnitIds(List<InvoiceGenerationTargetQueryResult> targets) {
		return targets.stream()
				.map(InvoiceGenerationTargetQueryResult::unitId)
				.toList();
	}

	private List<Invoice> toInvoices(List<InvoiceGenerationTargetQueryResult> targets, LocalDate billingMonth) {
		return targets.stream()
				.map(target -> toInvoice(target, billingMonth))
				.toList();
	}

	private Invoice toInvoice(InvoiceGenerationTargetQueryResult target, LocalDate billingMonth) {
		return new Invoice(
				UUID.randomUUID(),
				unitRepository.getReferenceById(target.unitId()),
				tenantRepository.getReferenceById(target.tenantId()),
				billingMonth,
				toInvoiceNumber(target, billingMonth),
				new BigDecimal(target.monthlyFee()).toPlainString(),
				billingMonth.withDayOfMonth(target.dueDay()),
				INITIAL_INVOICE_STATUS
		);
	}

	private String toInvoiceNumber(InvoiceGenerationTargetQueryResult target, LocalDate billingMonth) {
		String unitIdPart = target.unitId().toString().replace("-", "").toUpperCase();

		return "INV-" + YearMonth.from(billingMonth).toString().replace("-", "") + "-" + unitIdPart;
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

	private InvoiceShowResponse toShowResponse(InvoiceShowQueryResult invoice) {
		return new InvoiceShowResponse(
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
