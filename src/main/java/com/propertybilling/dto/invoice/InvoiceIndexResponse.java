package com.propertybilling.dto.invoice;

import com.propertybilling.dto.common.IndexResponse;
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
) implements IndexResponse {

	/**
	 * Builds a response with count derived from the returned invoice records.
	 *
	 * @param invoices returned invoice records
	 */
	public InvoiceIndexResponse(List<InvoiceIndexElement> invoices) {
		this(IndexResponse.countItems(invoices), invoices);
	}
}
