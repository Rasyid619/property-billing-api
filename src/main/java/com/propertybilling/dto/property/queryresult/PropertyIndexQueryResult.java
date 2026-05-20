package com.propertybilling.dto.property.queryresult;

import java.util.UUID;

/**
 * Query result for property index rows.
 *
 * @param id unique property identifier
 * @param name property display name
 * @param address optional property address
 * @param active whether the property is active
 */
public record PropertyIndexQueryResult(
		UUID id,
		String name,
		String address,
		boolean active
) {
}
