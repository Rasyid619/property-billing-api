package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.propertybilling.dto.cashbalance.CashBalanceCloseMonthRequest;
import com.propertybilling.entity.CashBalance;
import com.propertybilling.entity.Property;
import com.propertybilling.exception.CashBalanceConflictException;
import com.propertybilling.exception.CashBalanceMonthException;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.CashBalanceRepository;
import com.propertybilling.repository.PaymentRepository;
import com.propertybilling.repository.PropertyExpenseRepository;
import com.propertybilling.repository.PropertyRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

/*
 * Unit tests for cash balance closing workflows.
 */
class CashBalanceServiceTest {

	private final CashBalanceRepository cashBalanceRepository = Mockito.mock(CashBalanceRepository.class);
	private final PaymentRepository paymentRepository = Mockito.mock(PaymentRepository.class);
	private final PropertyExpenseRepository propertyExpenseRepository = Mockito.mock(PropertyExpenseRepository.class);
	private final PropertyRepository propertyRepository = Mockito.mock(PropertyRepository.class);
	private final CashBalanceService cashBalanceService = new CashBalanceService(
			cashBalanceRepository,
			paymentRepository,
			propertyExpenseRepository,
			propertyRepository
	);

	@Test
	void closesMonthWithPreviousClosingBalance() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		Property property = new Property(propertyId, "Green Residence", null, true);
		when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
		when(cashBalanceRepository.existsByPropertyIdAndMonth(propertyId, LocalDate.parse("2026-05-01")))
				.thenReturn(false);
		when(cashBalanceRepository.findClosingBalanceByPropertyIdAndMonth(
				propertyId,
				LocalDate.parse("2026-04-01")
		)).thenReturn(Optional.of("250000.00"));
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

		cashBalanceService.closeMonth(new CashBalanceCloseMonthRequest(
				propertyId,
				LocalDate.parse("2026-05-01")
		));

		ArgumentCaptor<CashBalance> cashBalanceCaptor = ArgumentCaptor.forClass(CashBalance.class);
		verify(cashBalanceRepository, times(1)).saveAndFlush(cashBalanceCaptor.capture());
		CashBalance cashBalance = cashBalanceCaptor.getValue();
		assertThat(cashBalance.getId()).isNotNull();
		assertThat(cashBalance.getProperty()).isEqualTo(property);
		assertThat(cashBalance.getMonth()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(cashBalance.getOpeningBalance()).isEqualTo("250000.00");
		assertThat(cashBalance.getTotalIncome()).isEqualTo("1500000.00");
		assertThat(cashBalance.getTotalExpense()).isEqualTo("400000.00");
		assertThat(cashBalance.getClosingBalance()).isEqualTo("1350000.00");
	}

	@Test
	void closesMonthWithZeroOpeningWhenPreviousBalanceDoesNotExist() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		Property property = new Property(propertyId, "Green Residence", null, true);
		when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
		when(cashBalanceRepository.existsByPropertyIdAndMonth(propertyId, LocalDate.parse("2026-05-01")))
				.thenReturn(false);
		when(cashBalanceRepository.findClosingBalanceByPropertyIdAndMonth(
				propertyId,
				LocalDate.parse("2026-04-01")
		)).thenReturn(Optional.empty());
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

		cashBalanceService.closeMonth(new CashBalanceCloseMonthRequest(
				propertyId,
				LocalDate.parse("2026-05-01")
		));

		ArgumentCaptor<CashBalance> cashBalanceCaptor = ArgumentCaptor.forClass(CashBalance.class);
		verify(cashBalanceRepository, times(1)).saveAndFlush(cashBalanceCaptor.capture());
		assertThat(cashBalanceCaptor.getValue().getOpeningBalance()).isEqualTo("0");
		assertThat(cashBalanceCaptor.getValue().getClosingBalance()).isEqualTo("0");
	}

	@Test
	void rejectsMonthThatIsNotFirstDay() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");

		assertThatThrownBy(() -> cashBalanceService.closeMonth(new CashBalanceCloseMonthRequest(
				propertyId,
				LocalDate.parse("2026-05-02")
		))).isInstanceOf(CashBalanceMonthException.class);

		verifyNoDatabaseWorkAfterValidationFailure();
	}

	@Test
	void throwsNotFoundWhenPropertyDoesNotExist() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cashBalanceService.closeMonth(new CashBalanceCloseMonthRequest(
				propertyId,
				LocalDate.parse("2026-05-01")
		))).isInstanceOf(PropertyNotFoundException.class);

		verify(propertyRepository, times(1)).findById(propertyId);
		verify(cashBalanceRepository, never()).saveAndFlush(any());
	}

	@Test
	void throwsConflictWhenMonthAlreadyClosed() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		Property property = new Property(propertyId, "Green Residence", null, true);
		when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
		when(cashBalanceRepository.existsByPropertyIdAndMonth(propertyId, LocalDate.parse("2026-05-01")))
				.thenReturn(true);

		assertThatThrownBy(() -> cashBalanceService.closeMonth(new CashBalanceCloseMonthRequest(
				propertyId,
				LocalDate.parse("2026-05-01")
		))).isInstanceOf(CashBalanceConflictException.class);

		verify(cashBalanceRepository, times(1)).existsByPropertyIdAndMonth(
				propertyId,
				LocalDate.parse("2026-05-01")
		);
		verify(cashBalanceRepository, never()).saveAndFlush(any());
	}

	@Test
	void throwsConflictWhenUniqueConstraintFailsDuringSave() {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		Property property = new Property(propertyId, "Green Residence", null, true);
		when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
		when(cashBalanceRepository.existsByPropertyIdAndMonth(propertyId, LocalDate.parse("2026-05-01")))
				.thenReturn(false);
		when(cashBalanceRepository.findClosingBalanceByPropertyIdAndMonth(
				propertyId,
				LocalDate.parse("2026-04-01")
		)).thenReturn(Optional.empty());
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
		when(cashBalanceRepository.saveAndFlush(any()))
				.thenThrow(new DataIntegrityViolationException("duplicate cash balance"));

		assertThatThrownBy(() -> cashBalanceService.closeMonth(new CashBalanceCloseMonthRequest(
				propertyId,
				LocalDate.parse("2026-05-01")
		))).isInstanceOf(CashBalanceConflictException.class);
	}

	private void verifyNoDatabaseWorkAfterValidationFailure() {
		verifyNoInteractions(
				propertyRepository,
				cashBalanceRepository,
				paymentRepository,
				propertyExpenseRepository
		);
	}
}
