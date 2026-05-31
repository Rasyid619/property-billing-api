package com.propertybilling.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.propertybilling.constant.InvoiceStatus;
import com.propertybilling.constant.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Payment item returned by the payment index endpoint.
 *
 * @param id unique payment identifier
 * @param invoiceId paid invoice identifier
 * @param amount payment amount
 * @param paymentDate date the payment was made
 * @param paymentMethod payment method
 * @param referenceNumber optional external reference number
 * @param note optional payment note
 * @param invoiceStatus current invoice status
 */
public record PaymentIndexElement(
		UUID id,
		@JsonProperty("invoice_id")
		UUID invoiceId,
		BigDecimal amount,
		@JsonProperty("payment_date")
		LocalDate paymentDate,
		@JsonProperty("payment_method")
		PaymentMethod paymentMethod,
		@JsonProperty("reference_number")
		String referenceNumber,
		String note,
		@JsonProperty("invoice_status")
		InvoiceStatus invoiceStatus
) {
}
