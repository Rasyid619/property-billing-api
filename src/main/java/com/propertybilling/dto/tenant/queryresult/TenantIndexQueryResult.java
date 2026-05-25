package com.propertybilling.dto.tenant.queryresult;

import java.util.UUID;

/**
 * Projection row used when listing tenants.
 *
 * @param id unique tenant identifier
 * @param name tenant display name
 * @param phone optional phone number
 * @param email optional email address
 */
public record TenantIndexQueryResult(
		UUID id,
		String name,
		String phone,
		String email
) {
}
