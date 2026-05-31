package com.propertybilling.service;

import com.propertybilling.dto.invoice.InvoiceGenerateMonthlyRequest;
import com.propertybilling.exception.InvoiceGenerationConflictException;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.PropertyRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.invoice.automation", name = "enabled", havingValue = "true")
/*
 * Internal scheduler that creates next-month invoices for active properties.
 */
public class InvoiceAutomationScheduler {

	private final PropertyRepository propertyRepository;
	private final InvoiceService invoiceService;
	private final Clock clock;

	/**
	 * Runs automated invoice generation using the configured cron schedule.
	 */
	@Scheduled(cron = "${app.invoice.automation.cron}", zone = "${app.invoice.automation.zone}")
	public void generateNextBillingMonthInvoices() {
		LocalDate billingMonth = getNextBillingMonth();
		List<UUID> activePropertyIds = propertyRepository.findActiveIdsForInvoiceAutomation();

		for (UUID propertyId : activePropertyIds) {
			generatePropertyInvoices(propertyId, billingMonth);
		}
	}

	private LocalDate getNextBillingMonth() {
		return YearMonth.now(clock)
				.plusMonths(1)
				.atDay(1);
	}

	private void generatePropertyInvoices(UUID propertyId, LocalDate billingMonth) {
		try {
			invoiceService.generateMonthlyInvoices(new InvoiceGenerateMonthlyRequest(propertyId, billingMonth));
		} catch (InvoiceGenerationConflictException | PropertyNotFoundException exception) {
			// Another run or manual trigger may already have generated this property month.
		}
	}
}
