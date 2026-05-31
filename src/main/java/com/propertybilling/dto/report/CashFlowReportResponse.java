package com.propertybilling.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cash-flow totals for one property month.
 *
 * @param propertyId owning property identifier
 * @param month report month in YYYY-MM format
 * @param totalIncome cash payments received in the month
 * @param totalExpense property expenses recorded in the month
 * @param netSaving income minus expenses
 */
public record CashFlowReportResponse(
		@JsonProperty("property_id")
		UUID propertyId,
		String month,
		@JsonProperty("total_income")
		BigDecimal totalIncome,
		@JsonProperty("total_expense")
		BigDecimal totalExpense,
		@JsonProperty("net_saving")
		BigDecimal netSaving
) {
}
