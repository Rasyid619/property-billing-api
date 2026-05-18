package com.propertybilling.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JWT tokens returned after a successful login.
 *
 * @param accessToken token used to authenticate API requests
 * @param refreshToken token used to obtain a later access token
 */
public record AuthTokenResponse(
		@JsonProperty("access_token")
		String accessToken,
		@JsonProperty("refresh_token")
		String refreshToken
) {
}
