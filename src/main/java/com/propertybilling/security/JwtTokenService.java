package com.propertybilling.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.propertybilling.entity.User;
import com.propertybilling.exception.InvalidAccessTokenException;
import com.propertybilling.exception.InvalidRefreshTokenException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
/**
 * Creates signed JWT access and refresh tokens for authenticated users.
 *
 * <p>TODO: Consider asymmetric signing when token verification needs to be shared
 * with other services without allowing them to mint tokens.
 */
public class JwtTokenService {

	private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Clock clock;
	private final String secret;
	private final long accessTokenTtlSeconds;
	private final long refreshTokenTtlSeconds;

	/**
	 * Creates a JWT token service from application configuration.
	 *
	 * @param secret signing secret used for HMAC tokens
	 * @param accessTokenTtlSeconds lifetime of access tokens in seconds
	 * @param refreshTokenTtlSeconds lifetime of refresh tokens in seconds
	 */
	@Autowired
	public JwtTokenService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds,
			@Value("${app.jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds
	) {
		this(Clock.systemUTC(), secret, accessTokenTtlSeconds, refreshTokenTtlSeconds);
	}

	JwtTokenService(
			Clock clock,
			String secret,
			long accessTokenTtlSeconds,
			long refreshTokenTtlSeconds
	) {
		this.clock = clock;
		this.secret = secret;
		this.accessTokenTtlSeconds = accessTokenTtlSeconds;
		this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
	}

	/**
	 * Creates an access token for an authenticated user.
	 *
	 * @param user authenticated user
	 * @return signed JWT access token
	 */
	public String createAccessToken(User user) {
		return createToken(user, "access", accessTokenTtlSeconds);
	}

	/**
	 * Creates a refresh token for an authenticated user.
	 *
	 * @param user authenticated user
	 * @return signed JWT refresh token
	 */
	public String createRefreshToken(User user) {
		return createToken(user, "refresh", refreshTokenTtlSeconds);
	}

	/**
	 * Validates a refresh token and returns the user identifier stored in it.
	 *
	 * @param token submitted JWT refresh token
	 * @return user identifier stored in the token subject
	 * @throws InvalidRefreshTokenException when the token is invalid or expired
	 */
	public UUID readRefreshTokenSubject(String token) {
		return readTokenSubject(token, "refresh");
	}

	/**
	 * Validates an access token and returns the user identifier stored in it.
	 *
	 * @param token submitted JWT access token
	 * @return user identifier stored in the token subject
	 * @throws InvalidAccessTokenException when the token is invalid or expired
	 */
	public UUID readAccessTokenSubject(String token) {
		try {
			return readTokenSubject(token, "access");
		} catch (InvalidRefreshTokenException exception) {
			throw new InvalidAccessTokenException();
		}
	}

	private UUID readTokenSubject(String token, String expectedType) {
		String[] tokenParts = token.split("\\.", 3);

		if (tokenParts.length != 3) {
			throw new InvalidRefreshTokenException();
		}

		String content = tokenParts[0] + "." + tokenParts[1];

		if (!isValidSignature(content, tokenParts[2])) {
			throw new InvalidRefreshTokenException();
		}

		Map<String, Object> payload = decodePayload(tokenParts[1]);

		if (!expectedType.equals(payload.get("type"))) {
			throw new InvalidRefreshTokenException();
		}

		if (isExpired(payload.get("exp"))) {
			throw new InvalidRefreshTokenException();
		}

		return parseSubject(payload.get("sub"));
	}

	private String createToken(User user, String tokenType, long ttlSeconds) {
		Instant issuedAt = clock.instant();
		Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", user.getId().toString());
		payload.put("email", user.getEmail());
		payload.put("role", user.getRole());
		payload.put("type", tokenType);
		payload.put("iat", issuedAt.getEpochSecond());
		payload.put("exp", issuedAt.plusSeconds(ttlSeconds).getEpochSecond());

		String encodedHeader = encodeJson(header);
		String encodedPayload = encodeJson(payload);
		String content = encodedHeader + "." + encodedPayload;

		return content + "." + sign(content);
	}

	private String encodeJson(Object value) {
		try {
			return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Unable to serialize JWT content.", exception);
		}
	}

	private String sign(String content) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return BASE64_URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to sign JWT.", exception);
		}
	}

	private boolean isValidSignature(String content, String submittedSignature) {
		byte[] expectedSignature = sign(content).getBytes(StandardCharsets.UTF_8);
		byte[] actualSignature = submittedSignature.getBytes(StandardCharsets.UTF_8);
		return java.security.MessageDigest.isEqual(expectedSignature, actualSignature);
	}

	private Map<String, Object> decodePayload(String encodedPayload) {
		try {
			byte[] decodedPayload = Base64.getUrlDecoder().decode(encodedPayload);
			return objectMapper.readValue(decodedPayload, new TypeReference<>() {});
		} catch (Exception exception) {
			throw new InvalidRefreshTokenException();
		}
	}

	private boolean isExpired(Object expiration) {
		if (!(expiration instanceof Number expirationSeconds)) {
			throw new InvalidRefreshTokenException();
		}

		return clock.instant().getEpochSecond() >= expirationSeconds.longValue();
	}

	private UUID parseSubject(Object subject) {
		if (!(subject instanceof String subjectValue)) {
			throw new InvalidRefreshTokenException();
		}

		try {
			return UUID.fromString(subjectValue);
		} catch (IllegalArgumentException exception) {
			throw new InvalidRefreshTokenException();
		}
	}
}
