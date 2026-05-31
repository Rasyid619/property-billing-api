package com.propertybilling.dto.expense;

import java.util.List;

/**
 * Property expense list response with returned item count.
 *
 * @param count number of returned expenses
 * @param expenses property expense items
 */
public record ExpenseIndexResponse(
		int count,
		List<ExpenseIndexElement> expenses
) {
}
