package com.propertybilling.dto.invoice.queryresult;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Projection row used when showing one invoice.
 *
 * @param id unique invoice identifier
 * @param unitId billed unit identifier
 * @param tenantId billed tenant identifier
 * @param billingMonth first day of the billed month
 * @param invoiceNumber unique invoice number
 * @param amount decimal string invoice amount
 * @param dueDate invoice due date
 * @param status current invoice status
 */
public record InvoiceShowQueryResult(
		UUID id,
		UUID unitId,
		UUID tenantId,
		LocalDate billingMonth,
		String invoiceNumber,
		String amount,
		LocalDate dueDate,
		String status
) {
}
