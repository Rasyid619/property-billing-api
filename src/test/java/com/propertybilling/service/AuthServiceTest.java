package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.propertybilling.entity.User;
import com.propertybilling.dto.auth.AccessTokenResponse;
import com.propertybilling.dto.auth.AuthTokenResponse;
import com.propertybilling.dto.auth.LoginRequest;
import com.propertybilling.exception.InvalidCredentialsException;
import com.propertybilling.exception.InvalidRefreshTokenException;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for authentication business rules.
 */
class AuthServiceTest {

	private final UserRepository userRepository = Mockito.mock(UserRepository.class);
	private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
	private final JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
	private final AuthService authService = new AuthService(userRepository, passwordEncoder, jwtTokenService);

	@Test
	void returnsTokensWhenCredentialsAreValid() {
		User user = buildUser();
		LoginRequest request = new LoginRequest("admin@example.com", "password123");
		when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
		when(jwtTokenService.createAccessToken(user)).thenReturn("access-token");
		when(jwtTokenService.createRefreshToken(user)).thenReturn("refresh-token");

		AuthTokenResponse response = authService.login(request);

		assertThat(response.accessToken()).isEqualTo("access-token");
		assertThat(response.refreshToken()).isEqualTo("refresh-token");
	}

	@Test
	void rejectsUnknownEmail() {
		LoginRequest request = new LoginRequest("missing@example.com", "password123");
		when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void rejectsInvalidPassword() {
		User user = buildUser();
		LoginRequest request = new LoginRequest("admin@example.com", "wrong-password");
		when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong-password", user.getPasswordHash())).thenReturn(false);

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void rejectsUnsupportedRole() {
		User user = new User(
				UUID.fromString("00000000-0000-0000-0000-000000000002"),
				"tenant@example.com",
				"password-hash",
				"tenant"
		);
		LoginRequest request = new LoginRequest("tenant@example.com", "password123");
		when(userRepository.findByEmail("tenant@example.com")).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void refreshReturnsAccessTokenWhenRefreshTokenIsValid() {
		User user = buildUser();
		when(jwtTokenService.readRefreshTokenSubject("refresh-token")).thenReturn(user.getId());
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(jwtTokenService.createAccessToken(user)).thenReturn("new-access-token");

		AccessTokenResponse response = authService.refresh("Bearer refresh-token");

		assertThat(response.accessToken()).isEqualTo("new-access-token");
	}

	@Test
	void refreshRejectsMalformedAuthorizationHeader() {
		assertThatThrownBy(() -> authService.refresh("refresh-token"))
				.isInstanceOf(InvalidRefreshTokenException.class);
	}

	@Test
	void refreshRejectsMissingUser() {
		UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000003");
		when(jwtTokenService.readRefreshTokenSubject("refresh-token")).thenReturn(userId);
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.refresh("Bearer refresh-token"))
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
