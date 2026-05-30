package com.propertybilling.dto.invoice;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Invoice item returned by the invoice index endpoint.
 *
 * @param id unique invoice identifier
 * @param unitId billed unit identifier
 * @param tenantId billed tenant identifier
 * @param billingMonth first day of the billed month
 * @param invoiceNumber unique invoice number
 * @param amount invoice amount
 * @param dueDate invoice due date
 * @param status current invoice status
 */
public record InvoiceIndexElement(
		UUID id,
		@JsonProperty("unit_id")
		UUID unitId,
		@JsonProperty("tenant_id")
		UUID tenantId,
		@JsonProperty("billing_month")
		LocalDate billingMonth,
		@JsonProperty("invoice_number")
		String invoiceNumber,
		BigDecimal amount,
		@JsonProperty("due_date")
		LocalDate dueDate,
		String status
) {
}
