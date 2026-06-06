package com.propertybilling.dto.unit;

import com.propertybilling.dto.common.IndexResponse;
import java.util.List;

/**
 * List response for units.
 *
 * @param count number of returned units
 * @param units returned unit records
 */
public record UnitIndexResponse(
		int count,
		List<UnitIndexElement> units
) implements IndexResponse {

	/**
	 * Builds a response with count derived from the returned unit records.
	 *
	 * @param units returned unit records
	 */
	public UnitIndexResponse(List<UnitIndexElement> units) {
		this(IndexResponse.countItems(units), units);
	}
}
