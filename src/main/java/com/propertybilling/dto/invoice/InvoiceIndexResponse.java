package com.propertybilling.dto.invoice;

import java.util.List;

/**
 * List response for invoices.
 *
 * @param count number of returned invoices
 * @param invoices returned invoice records
 */
public record InvoiceIndexResponse(
		int count,
		List<InvoiceIndexElement> invoices
) {
}
