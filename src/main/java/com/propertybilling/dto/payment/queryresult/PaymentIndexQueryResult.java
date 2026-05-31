package com.propertybilling.dto.payment.queryresult;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Projection row used when listing invoice payments.
 *
 * @param id unique payment identifier
 * @param invoiceId paid invoice identifier
 * @param amount decimal string payment amount
 * @param paymentDate date the payment was made
 * @param paymentMethod payment method value
 * @param referenceNumber optional external reference number
 * @param note optional payment note
 * @param invoiceStatus current invoice status value
 */
public record PaymentIndexQueryResult(
		UUID id,
		UUID invoiceId,
		String amount,
		LocalDate paymentDate,
		String paymentMethod,
		String referenceNumber,
		String note,
		String invoiceStatus
) {
}
