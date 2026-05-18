package com.propertybilling.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.propertybilling.entity.User;
import com.propertybilling.exception.InvalidRefreshTokenException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for JWT creation and refresh-token validation.
 */
class JwtTokenServiceTest {

	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-18T00:00:00Z"), ZoneOffset.UTC);

	private final JwtTokenService jwtTokenService = new JwtTokenService(CLOCK, "test-secret", 900, 604800);

	@Test
	void readsSubjectFromValidRefreshToken() {
		User user = buildUser();
		String refreshToken = jwtTokenService.createRefreshToken(user);

		assertThat(jwtTokenService.readRefreshTokenSubject(refreshToken)).isEqualTo(user.getId());
	}

	@Test
	void rejectsAccessTokenAsRefreshToken() {
		String accessToken = jwtTokenService.createAccessToken(buildUser());

		assertThatThrownBy(() -> jwtTokenService.readRefreshTokenSubject(accessToken))
				.isInstanceOf(InvalidRefreshTokenException.class);
	}

	@Test
	void rejectsExpiredRefreshToken() {
		JwtTokenService expiredTokenService = new JwtTokenService(CLOCK, "test-secret", 900, 0);
		String refreshToken = expiredTokenService.createRefreshToken(buildUser());

		assertThatThrownBy(() -> jwtTokenService.readRefreshTokenSubject(refreshToken))
				.isInstanceOf(InvalidRefreshTokenException.class);
	}

	private User buildUser() {
		return new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"admin@example.com",
				"password-hash",
				"admin"
		);
	}
}
