package com.propertybilling.dto.tenant;

import com.propertybilling.dto.common.IndexResponse;
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
) implements IndexResponse {

	/**
	 * Builds a response with count derived from the returned tenant items.
	 *
	 * @param tenants returned tenant items
	 */
	public TenantIndexResponse(List<TenantIndexElement> tenants) {
		this(IndexResponse.countItems(tenants), tenants);
	}
}
