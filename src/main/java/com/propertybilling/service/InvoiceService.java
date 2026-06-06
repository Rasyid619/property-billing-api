package com.propertybilling.service;

import com.propertybilling.constant.InvoiceStatus;
import com.propertybilling.constant.PaymentMethod;
import com.propertybilling.dto.invoice.InvoiceGenerateMonthlyRequest;
import com.propertybilling.dto.invoice.InvoiceIndexElement;
import com.propertybilling.dto.invoice.InvoiceIndexResponse;
import com.propertybilling.dto.invoice.InvoiceShowResponse;
import com.propertybilling.dto.invoice.queryresult.InvoiceGenerationTargetQueryResult;
import com.propertybilling.dto.invoice.queryresult.InvoiceIndexQueryResult;
import com.propertybilling.dto.invoice.queryresult.InvoiceShowQueryResult;
import com.propertybilling.dto.payment.PaymentCreateRequest;
import com.propertybilling.dto.payment.PaymentIndexElement;
import com.propertybilling.dto.payment.PaymentIndexResponse;
import com.propertybilling.dto.payment.queryresult.PaymentIndexQueryResult;
import com.propertybilling.entity.CreditApplication;
import com.propertybilling.entity.Invoice;
import com.propertybilling.entity.Payment;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.TenantUnitCredit;
import com.propertybilling.exception.InvoiceGenerationConflictException;
import com.propertybilling.exception.InvoiceNotFoundException;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.CreditApplicationRepository;
import com.propertybilling.repository.InvoiceRepository;
import com.propertybilling.repository.PaymentRepository;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.TenantUnitCreditRepository;
import com.propertybilling.repository.UnitRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for invoice retrieval and generation.
 */
public class InvoiceService {

	private final InvoiceRepository invoiceRepository;
	private final PaymentRepository paymentRepository;
	private final CreditApplicationRepository creditApplicationRepository;
	private final PropertyRepository propertyRepository;
	private final TenantRepository tenantRepository;
	private final TenantUnitCreditRepository tenantUnitCreditRepository;
	private final UnitRepository unitRepository;
	private final Clock clock;

	private static final String INITIAL_INVOICE_STATUS = InvoiceStatus.UNPAID.value();

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

		List<Invoice> invoices = toInvoices(targets, request.billingMonth());

		try {
			invoiceRepository.saveAllAndFlush(invoices);
		} catch (DataIntegrityViolationException exception) {
			throw new InvoiceGenerationConflictException();
		}

		applyAvailableCreditToInvoices(invoices, LocalDate.now(clock));
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
		List<InvoiceIndexElement> invoices = invoiceRepository.findIndex(
				propertyId,
				unitId,
				tenantId,
				billingMonth,
				toStatusFilter(status),
				offset,
				limit
		)
				.stream()
				.map(this::toIndexElement)
				.toList();

		return new InvoiceIndexResponse(invoices);
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

	/**
	 * Lists payments recorded for one invoice.
	 *
	 * @param invoiceId invoice identifier
	 * @return payment index response
	 * @throws InvoiceNotFoundException when no invoice exists for the ID
	 */
	public PaymentIndexResponse listPayments(UUID invoiceId) {
		if (!invoiceRepository.existsById(invoiceId)) {
			throw new InvoiceNotFoundException();
		}

		List<PaymentIndexElement> payments = paymentRepository.findIndexByInvoiceId(invoiceId)
				.stream()
				.map(this::toPaymentIndexElement)
				.toList();

		return new PaymentIndexResponse(payments);
	}

	/**
	 * Records a payment and recalculates affected invoice statuses.
	 *
	 * @param invoiceId selected invoice identifier
	 * @param request payment request
	 * @throws InvoiceNotFoundException when no invoice exists for the ID
	 */
	@Transactional
	public void recordPayment(UUID invoiceId, PaymentCreateRequest request) {
		List<Invoice> lockedInvoices = invoiceRepository.findPaymentAllocationInvoicesForUpdate(
				invoiceId,
				toStatusValues(InvoiceStatus.openStatuses())
		);
		Invoice lockedSelectedInvoice = findSelectedInvoice(lockedInvoices, invoiceId)
				.orElseThrow(InvoiceNotFoundException::new);
		InvoiceSettlement settlement = getSettlement(lockedSelectedInvoice);
		BigDecimal outstandingAmount = getOutstandingAmount(lockedSelectedInvoice, settlement);
		BigDecimal surplusCreditAmount = getSurplusCreditAmount(request.amount(), outstandingAmount);

		paymentRepository.save(toPayment(lockedSelectedInvoice, request.amount(), request));
		updateAndSaveInvoiceStatus(
				lockedSelectedInvoice,
				settlement.paidAmount().add(request.amount()),
				settlement.creditAppliedAmount()
		);

		if (hasNoSurplusCredit(surplusCreditAmount)) {
			return;
		}

		TenantUnitCredit credit = findOrCreateCredit(lockedSelectedInvoice);
		credit.increaseBalance(surplusCreditAmount);
		tenantUnitCreditRepository.save(credit);
		applyCreditToInvoices(credit, lockedInvoices, invoiceId, request.paymentDate());
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
		InvoiceSettlement settlement = new InvoiceSettlement(invoice.paidAmount(), invoice.creditAppliedAmount());
		BigDecimal amount = new BigDecimal(invoice.amount());

		return new InvoiceIndexElement(
				invoice.id(),
				invoice.unitId(),
				invoice.tenantId(),
				invoice.billingMonth(),
				invoice.invoiceNumber(),
				amount,
				settlement.paidAmount(),
				settlement.creditAppliedAmount(),
				getAmountDue(amount, settlement),
				invoice.dueDate(),
				invoice.status()
		);
	}

	private InvoiceShowResponse toShowResponse(InvoiceShowQueryResult invoice) {
		InvoiceSettlement settlement = getSettlement(invoice.id());
		BigDecimal amount = new BigDecimal(invoice.amount());

		return new InvoiceShowResponse(
				invoice.id(),
				invoice.unitId(),
				invoice.tenantId(),
				invoice.billingMonth(),
				invoice.invoiceNumber(),
				amount,
				settlement.paidAmount(),
				settlement.creditAppliedAmount(),
				getAmountDue(amount, settlement),
				invoice.dueDate(),
				invoice.status()
		);
	}

	private void applyAvailableCreditToInvoices(List<Invoice> invoices, LocalDate appliedDate) {
		for (Invoice invoice : invoices) {
			tenantUnitCreditRepository.findByTenantIdAndUnitIdForUpdate(
					invoice.getTenant().getId(),
					invoice.getUnit().getId()
			).ifPresent(credit -> applyCreditToInvoices(credit, List.of(invoice), null, appliedDate));
		}
	}

	private void updateAndSaveInvoiceStatus(Invoice invoice) {
		InvoiceSettlement settlement = getSettlement(invoice);

		updateAndSaveInvoiceStatus(invoice, settlement.paidAmount(), settlement.creditAppliedAmount());
	}

	private void updateAndSaveInvoiceStatus(
			Invoice invoice,
			BigDecimal paidAmount,
			BigDecimal creditAppliedAmount
	) {
		BigDecimal invoiceAmount = new BigDecimal(invoice.getAmount());
		invoice.updateStatus(toInvoiceStatus(invoice, invoiceAmount, paidAmount.add(creditAppliedAmount)));
		invoiceRepository.save(invoice);
	}

	private InvoiceStatus toInvoiceStatus(Invoice invoice, BigDecimal invoiceAmount, BigDecimal settledAmount) {
		if (isFullySettled(settledAmount, invoiceAmount)) {
			return InvoiceStatus.PAID;
		}

		if (invoice.getDueDate().isBefore(LocalDate.now(clock))) {
			return InvoiceStatus.OVERDUE;
		}

		if (isPartiallySettled(settledAmount)) {
			return InvoiceStatus.PARTIAL;
		}

		return InvoiceStatus.UNPAID;
	}

	private List<String> toStatusValues(List<InvoiceStatus> statuses) {
		return statuses.stream()
				.map(InvoiceStatus::value)
				.toList();
	}

	private Optional<Invoice> findSelectedInvoice(List<Invoice> invoices, UUID invoiceId) {
		return invoices.stream()
				.filter(invoice -> invoice.getId().equals(invoiceId))
				.findFirst();
	}

	private Payment toPayment(
			Invoice invoice,
			BigDecimal amount,
			PaymentCreateRequest request
	) {
		return new Payment(
				UUID.randomUUID(),
				invoice,
				amount.toPlainString(),
				request.paymentDate(),
				request.paymentMethod().value(),
				request.referenceNumber(),
				request.note()
		);
	}

	private TenantUnitCredit findOrCreateCredit(Invoice invoice) {
		return tenantUnitCreditRepository.findByTenantIdAndUnitIdForUpdate(
				invoice.getTenant().getId(),
				invoice.getUnit().getId()
		).orElseGet(() -> new TenantUnitCredit(
				UUID.randomUUID(),
				invoice.getTenant(),
				invoice.getUnit(),
				BigDecimal.ZERO.toPlainString()
		));
	}

	private void applyCreditToInvoices(
			TenantUnitCredit credit,
			List<Invoice> invoices,
			UUID skippedInvoiceId,
			LocalDate appliedDate
	) {
		for (Invoice invoice : invoices) {
			if (skippedInvoiceId != null && invoice.getId().equals(skippedInvoiceId)) {
				continue;
			}

			if (credit.hasNoRemainingCredit()) {
				break;
			}

			applyCreditToInvoice(credit, invoice, appliedDate);
		}

		tenantUnitCreditRepository.save(credit);
	}

	private void applyCreditToInvoice(
			TenantUnitCredit credit,
			Invoice invoice,
			LocalDate appliedDate
	) {
		InvoiceSettlement settlement = getSettlement(invoice);
		BigDecimal outstandingAmount = getOutstandingAmount(invoice, settlement);

		if (hasNoOutstandingAmount(outstandingAmount)) {
			updateAndSaveInvoiceStatus(invoice, settlement.paidAmount(), settlement.creditAppliedAmount());

			return;
		}

		BigDecimal appliedAmount = credit.getBalanceAmount().min(outstandingAmount);
		credit.decreaseBalance(appliedAmount);
		creditApplicationRepository.save(toCreditApplication(credit, invoice, appliedAmount, appliedDate));
		updateAndSaveInvoiceStatus(
				invoice,
				settlement.paidAmount(),
				settlement.creditAppliedAmount().add(appliedAmount)
		);
	}

	private CreditApplication toCreditApplication(
			TenantUnitCredit credit,
			Invoice invoice,
			BigDecimal amount,
			LocalDate appliedDate
	) {
		return new CreditApplication(
				UUID.randomUUID(),
				credit,
				invoice,
				amount.toPlainString(),
				appliedDate
		);
	}

	private InvoiceSettlement getSettlement(Invoice invoice) {
		return getSettlement(invoice.getId());
	}

	private InvoiceSettlement getSettlement(UUID invoiceId) {
		return new InvoiceSettlement(
				zeroIfNull(paymentRepository.sumAmountByInvoiceId(invoiceId)),
				zeroIfNull(creditApplicationRepository.sumAmountByInvoiceId(invoiceId))
		);
	}

	private BigDecimal getOutstandingAmount(Invoice invoice, InvoiceSettlement settlement) {
		return getAmountDue(new BigDecimal(invoice.getAmount()), settlement);
	}

	private BigDecimal getAmountDue(BigDecimal invoiceAmount, InvoiceSettlement settlement) {
		return invoiceAmount
				.subtract(settlement.paidAmount())
				.subtract(settlement.creditAppliedAmount())
				.max(BigDecimal.ZERO);
	}

	private BigDecimal getSurplusCreditAmount(BigDecimal paymentAmount, BigDecimal outstandingAmount) {
		return paymentAmount.subtract(outstandingAmount).max(BigDecimal.ZERO);
	}

	private boolean hasNoSurplusCredit(BigDecimal surplusCreditAmount) {
		return isZeroOrNegative(surplusCreditAmount);
	}

	private boolean isFullySettled(BigDecimal settledAmount, BigDecimal invoiceAmount) {
		return isGreaterThanOrEqualTo(settledAmount, invoiceAmount);
	}

	private boolean isPartiallySettled(BigDecimal settledAmount) {
		return isPositive(settledAmount);
	}

	private boolean hasNoOutstandingAmount(BigDecimal outstandingAmount) {
		return isZeroOrNegative(outstandingAmount);
	}

	private boolean isPositive(BigDecimal amount) {
		return amount.signum() > 0;
	}

	private boolean isZeroOrNegative(BigDecimal amount) {
		return amount.signum() <= 0;
	}

	private boolean isGreaterThanOrEqualTo(BigDecimal left, BigDecimal right) {
		return left.compareTo(right) >= 0;
	}

	private BigDecimal zeroIfNull(BigDecimal amount) {
		if (amount == null) {
			return BigDecimal.ZERO;
		}

		return amount;
	}

	private PaymentIndexElement toPaymentIndexElement(PaymentIndexQueryResult payment) {
		return new PaymentIndexElement(
				payment.id(),
				payment.invoiceId(),
				new BigDecimal(payment.amount()),
				payment.paymentDate(),
				PaymentMethod.fromValue(payment.paymentMethod()),
				payment.referenceNumber(),
				payment.note(),
				InvoiceStatus.fromValue(payment.invoiceStatus())
		);
	}

	private record InvoiceSettlement(
			BigDecimal paidAmount,
			BigDecimal creditAppliedAmount
	) {
	}
}
