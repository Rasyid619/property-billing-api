package com.propertybilling.dto.unit;

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
) {
}
