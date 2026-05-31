package com.propertybilling.dto.payment;

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
) {
}
