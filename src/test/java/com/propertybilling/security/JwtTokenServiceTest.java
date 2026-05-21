package com.propertybilling.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.propertybilling.entity.User;
import com.propertybilling.exception.InvalidAccessTokenException;
import com.propertybilling.exception.InvalidRefreshTokenException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

/*
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
	void readsSubjectFromValidAccessToken() {
		User user = buildUser();
		String accessToken = jwtTokenService.createAccessToken(user);

		assertThat(jwtTokenService.readAccessTokenSubject(accessToken)).isEqualTo(user.getId());
	}

	@Test
	void rejectsRefreshTokenAsAccessToken() {
		String refreshToken = jwtTokenService.createRefreshToken(buildUser());

		assertThatThrownBy(() -> jwtTokenService.readAccessTokenSubject(refreshToken))
				.isInstanceOf(InvalidAccessTokenException.class);
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

	@Test
	void rejectsMalformedTokenWithTooFewParts() {
		assertThatThrownBy(() -> jwtTokenService.readRefreshTokenSubject("header.payload"))
				.isInstanceOf(InvalidRefreshTokenException.class);
	}

	@Test
	void rejectsTamperedToken() {
		String validToken = jwtTokenService.createRefreshToken(buildUser());
		String tampered = validToken.substring(0, validToken.lastIndexOf('.') + 1) + "tamperedsignature";

		assertThatThrownBy(() -> jwtTokenService.readRefreshTokenSubject(tampered))
				.isInstanceOf(InvalidRefreshTokenException.class);
	}

	@Test
	void rejectsTokenWithInvalidBase64Payload() throws Exception {
		String token = craftTokenWithRawPayload("!!!not-valid-base64!!!");

		assertThatThrownBy(() -> jwtTokenService.readRefreshTokenSubject(token))
				.isInstanceOf(InvalidRefreshTokenException.class);
	}

	@Test
	void rejectsTokenWithStringExpiration() throws Exception {
		String token = craftToken(
				"{\"sub\":\"00000000-0000-0000-0000-000000000001\","
				+ "\"type\":\"refresh\",\"iat\":1000000000,\"exp\":\"not-a-number\"}"
		);

		assertThatThrownBy(() -> jwtTokenService.readRefreshTokenSubject(token))
				.isInstanceOf(InvalidRefreshTokenException.class);
	}

	@Test
	void rejectsTokenWithNonStringSubject() throws Exception {
		String token = craftToken(
				"{\"sub\":12345,\"type\":\"refresh\",\"iat\":1000000000,\"exp\":9999999999}"
		);

		assertThatThrownBy(() -> jwtTokenService.readRefreshTokenSubject(token))
				.isInstanceOf(InvalidRefreshTokenException.class);
	}

	@Test
	void rejectsTokenWithNonUuidSubject() throws Exception {
		String token = craftToken(
				"{\"sub\":\"not-a-uuid\",\"type\":\"refresh\",\"iat\":1000000000,\"exp\":9999999999}"
		);

		assertThatThrownBy(() -> jwtTokenService.readRefreshTokenSubject(token))
				.isInstanceOf(InvalidRefreshTokenException.class);
	}

	private String craftToken(String payloadJson) throws Exception {
		Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
		String encodedPayload = encoder.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
		return craftTokenWithRawPayload(encodedPayload);
	}

	private String craftTokenWithRawPayload(String rawPayloadBase64) throws Exception {
		Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
		String header = encoder.encodeToString(
				"{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8)
		);
		String content = header + "." + rawPayloadBase64;
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec("test-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		String signature = encoder.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
		return content + "." + signature;
	}

	private User buildUser() {
		return new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"Admin User",
				"admin@example.com",
				"password-hash",
				"admin"
		);
	}
}
