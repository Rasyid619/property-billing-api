package com.propertybilling.service;

import com.propertybilling.entity.User;
import com.propertybilling.dto.auth.AccessTokenResponse;
import com.propertybilling.dto.auth.AuthTokenResponse;
import com.propertybilling.dto.auth.LoginRequest;
import com.propertybilling.exception.InvalidCredentialsException;
import com.propertybilling.exception.InvalidRefreshTokenException;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
/**
 * Business workflow for authenticating admin and staff users.
 */
public class AuthService {

	private static final String ADMIN_ROLE = "admin";
	private static final String STAFF_ROLE = "staff";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenService;

	/**
	 * Creates the authentication workflow service.
	 *
	 * @param userRepository user data access boundary
	 * @param passwordEncoder password verifier
	 * @param jwtTokenService token issuer
	 */
	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenService jwtTokenService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenService = jwtTokenService;
	}

	/**
	 * Authenticates a user and issues access and refresh tokens.
	 *
	 * @param request submitted login credentials
	 * @return issued JWT tokens
	 * @throws InvalidCredentialsException when the credentials do not match a user
	 */
	public AuthTokenResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(InvalidCredentialsException::new);

		if (!canLogin(user)) {
			throw new InvalidCredentialsException();
		}

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		return new AuthTokenResponse(
				jwtTokenService.createAccessToken(user),
				jwtTokenService.createRefreshToken(user)
		);
	}

	/**
	 * Issues a new access token from a valid refresh token.
	 *
	 * @param authorizationHeader bearer refresh token header
	 * @return newly issued access token
	 * @throws InvalidRefreshTokenException when the refresh token is invalid
	 */
	public AccessTokenResponse refresh(String authorizationHeader) {
		String refreshToken = extractBearerToken(authorizationHeader);
		User user = userRepository.findById(jwtTokenService.readRefreshTokenSubject(refreshToken))
				.orElseThrow(InvalidRefreshTokenException::new);

		if (!canLogin(user)) {
			throw new InvalidRefreshTokenException();
		}

		return new AccessTokenResponse(jwtTokenService.createAccessToken(user));
	}

	private boolean canLogin(User user) {
		return ADMIN_ROLE.equals(user.getRole()) || STAFF_ROLE.equals(user.getRole());
	}

	private String extractBearerToken(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			throw new InvalidRefreshTokenException();
		}

		String token = authorizationHeader.substring("Bearer ".length());

		if (token.isBlank()) {
			throw new InvalidRefreshTokenException();
		}

		return token;
	}
}
