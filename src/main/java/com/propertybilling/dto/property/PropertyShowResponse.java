package com.propertybilling.dto.property;

import java.util.UUID;

/**
 * Detail response for one property.
 *
 * @param id unique property identifier
 * @param name display name
 * @param address optional property address
 * @param active whether the property can be used for new workflows
 */
public record PropertyShowResponse(
		UUID id,
		String name,
		String address,
		boolean active
) {
}
