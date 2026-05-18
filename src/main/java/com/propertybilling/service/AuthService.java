package com.propertybilling.service;

import com.propertybilling.domain.User;
import com.propertybilling.dto.auth.AuthTokenResponse;
import com.propertybilling.dto.auth.LoginRequest;
import com.propertybilling.exception.InvalidCredentialsException;
import com.propertybilling.repository.UserRepository;
import com.propertybilling.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenService;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenService jwtTokenService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenService = jwtTokenService;
	}

	public AuthTokenResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		return new AuthTokenResponse(
				jwtTokenService.createAccessToken(user),
				jwtTokenService.createRefreshToken(user)
		);
	}
}
