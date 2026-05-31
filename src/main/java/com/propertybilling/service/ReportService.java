package com.propertybilling.service;

import com.propertybilling.dto.report.CashFlowReportResponse;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.PaymentRepository;
import com.propertybilling.repository.PropertyExpenseRepository;
import com.propertybilling.repository.PropertyRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for reporting endpoints.
 */
public class ReportService {

	private final PropertyRepository propertyRepository;
	private final PaymentRepository paymentRepository;
	private final PropertyExpenseRepository propertyExpenseRepository;

	/**
	 * Gets cash-flow totals for one property month.
	 *
	 * @param propertyId owning property identifier
	 * @param month report month in YYYY-MM format
	 * @return cash-flow report response
	 * @throws PropertyNotFoundException when no property exists for the request
	 */
	public CashFlowReportResponse getCashFlowReport(UUID propertyId, String month) {
		if (!propertyRepository.existsById(propertyId)) {
			throw new PropertyNotFoundException();
		}

		LocalDate monthStart = YearMonth.parse(month).atDay(1);
		LocalDate nextMonthStart = monthStart.plusMonths(1);
		BigDecimal totalIncome = paymentRepository.sumCashIncomeByPropertyIdAndPaymentDateRange(
				propertyId,
				monthStart,
				nextMonthStart
		);
		BigDecimal totalExpense = propertyExpenseRepository.sumAmountByPropertyIdAndExpenseDateRange(
				propertyId,
				monthStart,
				nextMonthStart
		);

		return new CashFlowReportResponse(
				propertyId,
				month,
				totalIncome,
				totalExpense,
				totalIncome.subtract(totalExpense)
		);
	}
}
