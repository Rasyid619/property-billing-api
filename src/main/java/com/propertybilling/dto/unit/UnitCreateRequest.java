package com.propertybilling.dto.unit;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a unit inside one property.
 *
 * @param unitNumber unit number unique inside the property
 * @param monthlyFee monthly fee decimal string greater than zero
 * @param dueDay day of month when payment is due
 */
public record UnitCreateRequest(
		@JsonProperty("unit_number")
		@NotBlank
		@Size(max = 50)
		String unitNumber,
		@JsonProperty("monthly_fee")
		@NotBlank
		@Pattern(regexp = "^(?!0+(?:\\.0+)?$)\\d+(?:\\.\\d{1,2})?$")
		String monthlyFee,
		@JsonProperty("due_day")
		@NotNull
		@Min(1)
		@Max(28)
		Integer dueDay
) {
}
