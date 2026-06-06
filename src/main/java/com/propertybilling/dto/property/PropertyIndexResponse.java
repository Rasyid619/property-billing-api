package com.propertybilling.dto.property;

import com.propertybilling.dto.common.IndexResponse;
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
) implements IndexResponse {

	/**
	 * Builds a response with count derived from the returned property items.
	 *
	 * @param properties returned property items
	 */
	public PropertyIndexResponse(List<PropertyIndexElement> properties) {
		this(IndexResponse.countItems(properties), properties);
	}
}
