package com.propertybilling.dto.cashbalance;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request payload for closing a property month cash balance.
 *
 * @param propertyId owning property identifier
 * @param month first day of the month to close
 */
public record CashBalanceCloseMonthRequest(
		@NotNull
		@JsonProperty("property_id")
		UUID propertyId,
		@NotNull
		LocalDate month
) {
}
