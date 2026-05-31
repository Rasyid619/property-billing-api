package com.propertybilling.service;

import com.propertybilling.dto.expense.ExpenseIndexElement;
import com.propertybilling.dto.expense.ExpenseIndexResponse;
import com.propertybilling.dto.expense.queryresult.ExpenseIndexQueryResult;
import com.propertybilling.repository.PropertyExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for property expense tracking.
 */
public class PropertyExpenseService {

	private final PropertyExpenseRepository propertyExpenseRepository;

	/**
	 * Lists expenses for one property with optional month filtering.
	 *
	 * @param propertyId owning property identifier
	 * @param month optional month in YYYY-MM format
	 * @param offset number of expenses to skip
	 * @param limit maximum number of expenses to return
	 * @return property expense index response
	 */
	public ExpenseIndexResponse listExpenses(UUID propertyId, String month, int offset, int limit) {
		LocalDate monthStart = toMonthStart(month);
		LocalDate nextMonthStart = toNextMonthStart(monthStart);
		List<ExpenseIndexElement> expenses = propertyExpenseRepository.findIndex(
				propertyId,
				monthStart,
				nextMonthStart,
				offset,
				limit
		)
				.stream()
				.map(this::toIndexElement)
				.toList();

		return new ExpenseIndexResponse(expenses.size(), expenses);
	}

	private LocalDate toMonthStart(String month) {
		if (month == null || month.isBlank()) {
			return null;
		}

		return YearMonth.parse(month).atDay(1);
	}

	private LocalDate toNextMonthStart(LocalDate monthStart) {
		if (monthStart == null) {
			return null;
		}

		return monthStart.plusMonths(1);
	}

	private ExpenseIndexElement toIndexElement(ExpenseIndexQueryResult expense) {
		return new ExpenseIndexElement(
				expense.getId(),
				expense.getPropertyId(),
				expense.getExpenseDate(),
				expense.getCategory(),
				new BigDecimal(expense.getAmount()),
				expense.getDescription()
		);
	}
}
