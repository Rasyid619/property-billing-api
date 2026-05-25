package com.propertybilling.dto.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a tenant data record.
 *
 * @param name tenant display name
 * @param phone optional phone number
 * @param email optional email address
 */
public record TenantCreateRequest(
		@NotBlank
		@Size(max = 150)
		String name,
		@Size(max = 30)
		String phone,
		@Email
		@Size(max = 150)
		String email
) {
}
