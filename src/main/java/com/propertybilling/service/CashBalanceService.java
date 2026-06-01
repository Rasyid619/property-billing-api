package com.propertybilling.service;

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
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for monthly cash balance closings.
 */
public class CashBalanceService {

	private static final BigDecimal ZERO_BALANCE = BigDecimal.ZERO;

	private final CashBalanceRepository cashBalanceRepository;
	private final PaymentRepository paymentRepository;
	private final PropertyExpenseRepository propertyExpenseRepository;
	private final PropertyRepository propertyRepository;

	/**
	 * Closes a property month cash balance.
	 *
	 * @param request close-month request
	 * @throws CashBalanceMonthException when month is not the first day
	 * @throws PropertyNotFoundException when no property exists for the request
	 * @throws CashBalanceConflictException when the property month is already closed
	 */
	@Transactional
	public void closeMonth(CashBalanceCloseMonthRequest request) {
		if (!isFirstDayOfMonth(request.month())) {
			throw new CashBalanceMonthException();
		}

		Property property = propertyRepository.findByIdForUpdate(request.propertyId())
				.orElseThrow(PropertyNotFoundException::new);

		if (cashBalanceRepository.existsByPropertyIdAndMonth(request.propertyId(), request.month())) {
			throw new CashBalanceConflictException();
		}

		BigDecimal openingBalance = getOpeningBalance(request.propertyId(), request.month());
		BigDecimal totalIncome = getTotalIncome(request.propertyId(), request.month());
		BigDecimal totalExpense = getTotalExpense(request.propertyId(), request.month());
		BigDecimal closingBalance = openingBalance.add(totalIncome).subtract(totalExpense);

		try {
			cashBalanceRepository.saveAndFlush(new CashBalance(
					UUID.randomUUID(),
					property,
					request.month(),
					openingBalance.toPlainString(),
					totalIncome.toPlainString(),
					totalExpense.toPlainString(),
					closingBalance.toPlainString()
			));
		} catch (DataIntegrityViolationException exception) {
			throw new CashBalanceConflictException();
		}
	}

	private boolean isFirstDayOfMonth(LocalDate month) {
		return month.getDayOfMonth() == 1;
	}

	private BigDecimal getOpeningBalance(UUID propertyId, LocalDate month) {
		return cashBalanceRepository.findClosingBalanceByPropertyIdAndMonth(propertyId, month.minusMonths(1))
				.map(BigDecimal::new)
				.orElse(ZERO_BALANCE);
	}

	private BigDecimal getTotalIncome(UUID propertyId, LocalDate month) {
		return paymentRepository.sumCashIncomeByPropertyIdAndPaymentDateRange(
				propertyId,
				month,
				month.plusMonths(1)
		);
	}

	private BigDecimal getTotalExpense(UUID propertyId, LocalDate month) {
		return propertyExpenseRepository.sumAmountByPropertyIdAndExpenseDateRange(
				propertyId,
				month,
				month.plusMonths(1)
		);
	}
}
