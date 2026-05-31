package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.propertybilling.dto.report.CashFlowReportResponse;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.PaymentRepository;
import com.propertybilling.repository.PropertyExpenseRepository;
import com.propertybilling.repository.PropertyRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/*
 * Unit tests for reporting workflows.
 */
class ReportServiceTest {

	private final PropertyRepository propertyRepository = Mockito.mock(PropertyRepository.class);
	private final PaymentRepository paymentRepository = Mockito.mock(PaymentRepository.class);
	private final PropertyExpenseRepository propertyExpenseRepository = Mockito.mock(PropertyExpenseRepository.class);
	private final ReportService reportService = new ReportService(
			propertyRepository,
			paymentRepository,
			propertyExpenseRepository
	);

	@Test
	void getsCashFlowReport() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		when(propertyRepository.existsById(propertyId)).thenReturn(true);
		when(paymentRepository.sumCashIncomeByPropertyIdAndPaymentDateRange(
				propertyId,
				LocalDate.parse("2026-05-01"),
				LocalDate.parse("2026-06-01")
		)).thenReturn(new BigDecimal("1500000.00"));
		when(propertyExpenseRepository.sumAmountByPropertyIdAndExpenseDateRange(
				propertyId,
				LocalDate.parse("2026-05-01"),
				LocalDate.parse("2026-06-01")
		)).thenReturn(new BigDecimal("400000.00"));

		CashFlowReportResponse response = reportService.getCashFlowReport(propertyId, "2026-05");

		assertThat(response.propertyId()).isEqualTo(propertyId);
		assertThat(response.month()).isEqualTo("2026-05");
		assertThat(response.totalIncome()).isEqualByComparingTo("1500000.00");
		assertThat(response.totalExpense()).isEqualByComparingTo("400000.00");
		assertThat(response.netSaving()).isEqualByComparingTo("1100000.00");
		verify(propertyRepository, times(1)).existsById(propertyId);
		verify(paymentRepository, times(1)).sumCashIncomeByPropertyIdAndPaymentDateRange(
				propertyId,
				LocalDate.parse("2026-05-01"),
				LocalDate.parse("2026-06-01")
		);
		verify(propertyExpenseRepository, times(1)).sumAmountByPropertyIdAndExpenseDateRange(
				propertyId,
				LocalDate.parse("2026-05-01"),
				LocalDate.parse("2026-06-01")
		);
	}

	@Test
	void returnsZeroTotalsWhenNoIncomeOrExpensesExist() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		when(propertyRepository.existsById(propertyId)).thenReturn(true);
		when(paymentRepository.sumCashIncomeByPropertyIdAndPaymentDateRange(
				propertyId,
				LocalDate.parse("2026-05-01"),
				LocalDate.parse("2026-06-01")
		)).thenReturn(BigDecimal.ZERO);
		when(propertyExpenseRepository.sumAmountByPropertyIdAndExpenseDateRange(
				propertyId,
				LocalDate.parse("2026-05-01"),
				LocalDate.parse("2026-06-01")
		)).thenReturn(BigDecimal.ZERO);

		CashFlowReportResponse response = reportService.getCashFlowReport(propertyId, "2026-05");

		assertThat(response.totalIncome()).isZero();
		assertThat(response.totalExpense()).isZero();
		assertThat(response.netSaving()).isZero();
	}

	@Test
	void throwsNotFoundWhenPropertyDoesNotExist() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		when(propertyRepository.existsById(propertyId)).thenReturn(false);

		assertThatThrownBy(() -> reportService.getCashFlowReport(propertyId, "2026-05"))
				.isInstanceOf(PropertyNotFoundException.class);

		verify(propertyRepository, times(1)).existsById(propertyId);
		verify(paymentRepository, never()).sumCashIncomeByPropertyIdAndPaymentDateRange(
				Mockito.any(),
				Mockito.any(),
				Mockito.any()
		);
		verify(propertyExpenseRepository, never()).sumAmountByPropertyIdAndExpenseDateRange(
				Mockito.any(),
				Mockito.any(),
				Mockito.any()
		);
	}
}
