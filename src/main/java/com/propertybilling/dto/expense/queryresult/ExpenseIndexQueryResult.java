package com.propertybilling.dto.expense.queryresult;

import java.time.LocalDate;
import java.util.UUID;

/*
 * Projection row used when listing property expenses.
 */
public interface ExpenseIndexQueryResult {

	/**
	 * @return unique expense identifier
	 */
	UUID getId();

	/**
	 * @return owning property identifier
	 */
	UUID getPropertyId();

	/**
	 * @return date the expense happened
	 */
	LocalDate getExpenseDate();

	/**
	 * @return expense category
	 */
	String getCategory();

	/**
	 * @return decimal string expense amount
	 */
	String getAmount();

	/**
	 * @return optional expense description
	 */
	String getDescription();
}
