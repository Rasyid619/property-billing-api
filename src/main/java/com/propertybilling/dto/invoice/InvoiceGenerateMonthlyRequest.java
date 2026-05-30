package com.propertybilling.dto.invoice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request data for monthly invoice generation.
 *
 * @param propertyId property to generate invoices for
 * @param billingMonth first day of the billing month
 */
public record InvoiceGenerateMonthlyRequest(
		@NotNull
		@JsonProperty("property_id")
		UUID propertyId,
		@NotNull
		@JsonProperty("billing_month")
		LocalDate billingMonth
) {

	/**
	 * Checks that the billing month follows the first-day monthly storage rule.
	 *
	 * @return true when billing month is absent or uses day one
	 */
	@JsonIgnore
	@AssertTrue
	public boolean isBillingMonthFirstDay() {
		return billingMonth == null || billingMonth.getDayOfMonth() == 1;
	}
}
