package com.propertybilling.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.propertybilling.domain.User;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

	private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Clock clock;
	private final String secret;
	private final long accessTokenTtlSeconds;
	private final long refreshTokenTtlSeconds;

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

	public String createAccessToken(User user) {
		return createToken(user, "access", accessTokenTtlSeconds);
	}

	public String createRefreshToken(User user) {
		return createToken(user, "refresh", refreshTokenTtlSeconds);
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
}
