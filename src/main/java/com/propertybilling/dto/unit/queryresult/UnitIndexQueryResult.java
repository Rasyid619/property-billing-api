package com.propertybilling.dto.unit.queryresult;

import java.util.UUID;

/**
 * Projection row used when listing units.
 *
 * @param id unique unit identifier
 * @param propertyId owning property identifier
 * @param unitNumber display number unique inside one property
 * @param monthlyFee decimal string monthly fee
 * @param dueDay day of month when payment is due
 * @param active whether the unit can be used for new workflows
 */
public record UnitIndexQueryResult(
		UUID id,
		UUID propertyId,
		String unitNumber,
		String monthlyFee,
		int dueDay,
		boolean active
) {
}
