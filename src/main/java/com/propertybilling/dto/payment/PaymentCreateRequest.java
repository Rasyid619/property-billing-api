package com.propertybilling.dto.payment;

import com.propertybilling.constant.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload for recording an invoice payment.
 *
 * @param amount positive payment amount
 * @param paymentDate date the payment was made
 * @param paymentMethod payment method label
 * @param referenceNumber optional external reference number
 * @param note optional payment note
 */
public record PaymentCreateRequest(
		@NotNull
		@DecimalMin(value = "0.01")
		BigDecimal amount,
		@NotNull
		@JsonProperty("payment_date")
		LocalDate paymentDate,
		@NotNull
		@JsonProperty("payment_method")
		PaymentMethod paymentMethod,
		@Size(max = 100)
		@JsonProperty("reference_number")
		String referenceNumber,
		@Size(max = 500)
		String note
) {
}
