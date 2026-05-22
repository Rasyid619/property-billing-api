package com.propertybilling.dto.unit;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Unit item returned by the unit index endpoint.
 *
 * @param id unique unit identifier
 * @param propertyId owning property identifier
 * @param unitNumber display number unique inside one property
 * @param monthlyFee monthly fee amount
 * @param dueDay day of month when payment is due
 * @param active whether the unit can be used for new workflows
 */
public record UnitIndexElement(
		UUID id,
		@JsonProperty("property_id")
		UUID propertyId,
		@JsonProperty("unit_number")
		String unitNumber,
		@JsonProperty("monthly_fee")
		BigDecimal monthlyFee,
		@JsonProperty("due_day")
		int dueDay,
		boolean active
) {
}
