package com.propertybilling.dto.expense;

import com.propertybilling.dto.common.IndexResponse;
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
) implements IndexResponse {

	/**
	 * Builds a response with count derived from the returned expense items.
	 *
	 * @param expenses property expense items
	 */
	public ExpenseIndexResponse(List<ExpenseIndexElement> expenses) {
		this(IndexResponse.countItems(expenses), expenses);
	}
}
