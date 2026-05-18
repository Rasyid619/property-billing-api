package com.propertybilling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthTokenResponse(
		@JsonProperty("access_token")
		String accessToken,
		@JsonProperty("refresh_token")
		String refreshToken
) {
}
