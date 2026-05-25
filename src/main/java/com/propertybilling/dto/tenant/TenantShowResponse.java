package com.propertybilling.dto.tenant;

import java.util.UUID;

/**
 * Detail response for one tenant data record.
 *
 * @param id unique tenant identifier
 * @param name tenant display name
 * @param phone optional phone number
 * @param email optional email address
 */
public record TenantShowResponse(
		UUID id,
		String name,
		String phone,
		String email
) {
}
