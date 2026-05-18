package com.propertybilling.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Credentials submitted by an admin or staff user during login.
 *
 * @param email account email address
 * @param password plain-text password to verify against the stored hash
 */
public record LoginRequest(
		@NotBlank
		@Email
		String email,
		@NotBlank
		@Size(min = 8)
		String password
) {
}
