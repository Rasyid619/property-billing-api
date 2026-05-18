package com.propertybilling.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JWT access token returned after a successful token refresh.
 *
 * @param accessToken token used to authenticate API requests
 */
public record AccessTokenResponse(
		@JsonProperty("access_token")
		String accessToken
) {
}
