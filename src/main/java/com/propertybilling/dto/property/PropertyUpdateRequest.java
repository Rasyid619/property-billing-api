package com.propertybilling.dto.property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for updating a property.
 *
 * @param name property display name
 * @param address optional property address
 */
public record PropertyUpdateRequest(
		@NotBlank
		@Size(max = 150)
		String name,
		@Size(max = 500)
		String address
) {
}
