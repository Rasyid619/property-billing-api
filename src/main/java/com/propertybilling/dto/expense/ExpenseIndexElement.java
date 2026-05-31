package com.propertybilling.dto.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One property expense item returned by the expense index endpoint.
 *
 * @param id unique expense identifier
 * @param propertyId owning property identifier
 * @param expenseDate date the expense happened
 * @param category expense category
 * @param amount expense amount
 * @param description optional expense description
 */
public record ExpenseIndexElement(
		UUID id,
		@JsonProperty("property_id") UUID propertyId,
		@JsonProperty("expense_date") LocalDate expenseDate,
		String category,
		BigDecimal amount,
		String description
) {
}
