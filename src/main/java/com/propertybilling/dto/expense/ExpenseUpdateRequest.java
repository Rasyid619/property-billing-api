package com.propertybilling.dto.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.propertybilling.constant.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request payload for replacing a property expense.
 *
 * @param propertyId owning property identifier
 * @param expenseDate date the expense happened
 * @param category supported expense category
 * @param amount positive expense amount
 * @param description optional expense description
 */
public record ExpenseUpdateRequest(
		@NotNull
		@JsonProperty("property_id")
		UUID propertyId,
		@NotNull
		@JsonProperty("expense_date")
		LocalDate expenseDate,
		@NotNull
		ExpenseCategory category,
		@NotNull
		@DecimalMin(value = "0.01")
		BigDecimal amount,
		@Size(max = 500)
		String description
) {
}
