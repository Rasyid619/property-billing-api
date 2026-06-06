package com.propertybilling.dto.payment;

import com.propertybilling.dto.common.IndexResponse;
import java.util.List;

/**
 * List response for invoice payments.
 *
 * @param count number of returned payments
 * @param payments returned payment records
 */
public record PaymentIndexResponse(
		int count,
		List<PaymentIndexElement> payments
) implements IndexResponse {

	/**
	 * Builds a response with count derived from the returned payment records.
	 *
	 * @param payments returned payment records
	 */
	public PaymentIndexResponse(List<PaymentIndexElement> payments) {
		this(IndexResponse.countItems(payments), payments);
	}
}
