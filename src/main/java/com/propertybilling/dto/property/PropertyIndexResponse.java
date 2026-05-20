package com.propertybilling.dto.property;

import java.util.List;

/**
 * List response for property index results.
 *
 * @param count number of property items returned
 * @param properties returned property items
 */
public record PropertyIndexResponse(
		int count,
		List<PropertyIndexElement> properties
) {
}
