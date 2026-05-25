package com.propertybilling.dto.tenant;

import java.util.List;

/**
 * List response for tenant index results.
 *
 * @param count number of tenant items returned
 * @param tenants returned tenant items
 */
public record TenantIndexResponse(
		int count,
		List<TenantIndexElement> tenants
) {
}
