package com.propertybilling.dto.tenant;

import java.util.UUID;

/**
 * Tenant item returned by the tenant index endpoint.
 *
 * @param id unique tenant identifier
 * @param name tenant display name
 * @param phone optional phone number
 * @param email optional email address
 */
public record TenantIndexElement(
		UUID id,
		String name,
		String phone,
		String email
) {
}
