package com.propertybilling.dto.invoice.queryresult;

import java.util.UUID;

/**
 * Projection row used when generating monthly invoices.
 *
 * @param unitId active unit identifier
 * @param tenantId active tenant identifier
 * @param unitNumber unit display number
 * @param monthlyFee decimal string unit monthly fee
 * @param dueDay day of month when payment is due
 */
public record InvoiceGenerationTargetQueryResult(
		UUID unitId,
		UUID tenantId,
		String unitNumber,
		String monthlyFee,
		int dueDay
) {
}
