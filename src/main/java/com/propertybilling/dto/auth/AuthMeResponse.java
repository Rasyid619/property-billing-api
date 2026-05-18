package com.propertybilling.dto.auth;

import java.util.UUID;

/**
 * Authenticated user details returned by the current-user endpoint.
 *
 * @param id unique user identifier
 * @param name display name
 * @param email login email address
 * @param role authorization role
 */
public record AuthMeResponse(
		UUID id,
		String name,
		String email,
		String role
) {
}
