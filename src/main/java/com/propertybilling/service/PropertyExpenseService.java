package com.propertybilling.service;

import com.propertybilling.dto.expense.ExpenseCreateRequest;
import com.propertybilling.dto.expense.ExpenseIndexElement;
import com.propertybilling.dto.expense.ExpenseIndexResponse;
import com.propertybilling.dto.expense.ExpenseUpdateRequest;
import com.propertybilling.dto.expense.queryresult.ExpenseIndexQueryResult;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.PropertyExpense;
import com.propertybilling.exception.PropertyExpenseNotFoundException;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.PropertyExpenseRepository;
import com.propertybilling.repository.PropertyRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for property expense tracking.
 */
public class PropertyExpenseService {

	private final PropertyRepository propertyRepository;
	private final PropertyExpenseRepository propertyExpenseRepository;

	/**
	 * Creates a property expense.
	 *
	 * @param request expense creation request
	 * @throws PropertyNotFoundException when no property exists for the request
	 */
	public void createExpense(ExpenseCreateRequest request) {
		Property property = propertyRepository.findById(request.propertyId())
				.orElseThrow(PropertyNotFoundException::new);

		propertyExpenseRepository.save(new PropertyExpense(
				UUID.randomUUID(),
				property,
				request.expenseDate(),
				request.category().value(),
				request.amount().toPlainString(),
				request.description(),
				null
		));
	}

	/**
	 * Replaces a property expense.
	 *
	 * @param expenseId property expense identifier
	 * @param request expense replacement request
	 * @throws PropertyExpenseNotFoundException when no expense exists for the ID
	 * @throws PropertyNotFoundException when no property exists for the request
	 */
	@Transactional
	public void updateExpense(UUID expenseId, ExpenseUpdateRequest request) {
		PropertyExpense expense = propertyExpenseRepository.findByIdForUpdate(expenseId)
				.orElseThrow(PropertyExpenseNotFoundException::new);
		Property property = propertyRepository.findById(request.propertyId())
				.orElseThrow(PropertyNotFoundException::new);

		expense.update(
				property,
				request.expenseDate(),
				request.category().value(),
				request.amount().toPlainString(),
				request.description()
		);
		propertyExpenseRepository.save(expense);
	}

	/**
	 * Deletes a property expense.
	 *
	 * @param expenseId property expense identifier
	 * @throws PropertyExpenseNotFoundException when no expense exists for the ID
	 */
	@Transactional
	public void deleteExpense(UUID expenseId) {
		PropertyExpense expense = propertyExpenseRepository.findByIdForUpdate(expenseId)
				.orElseThrow(PropertyExpenseNotFoundException::new);

		propertyExpenseRepository.delete(expense);
	}

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
