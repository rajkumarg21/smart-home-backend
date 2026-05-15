package com.rks.automation.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.rks.automation.dto.AuthResponse;
import com.rks.automation.dto.LoginRequest;
import com.rks.automation.dto.RegisterRequest;
import com.rks.automation.entity.Role;
import com.rks.automation.entity.User;
import com.rks.automation.repository.UserRepository;
import com.rks.automation.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and login, returning a signed JWT on success.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	// ── Register ─────────────────────────────────────────────────────────────

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new IllegalArgumentException("Username already taken: " + request.getUsername());
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email already registered: " + request.getEmail());
		}

		// Default to ROLE_USER; prevent self-assignment of ROLE_ADMIN
		Role role = (request.getRole() != null && request.getRole() != Role.ROLE_ADMIN) ? request.getRole()
				: Role.ROLE_USER;

		User user = User.builder().username(request.getUsername()).email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword())).fullName(request.getFullName()).role(role)
				.build();

		userRepository.save(user);

		String token = jwtTokenProvider.generateToken(user);
		return buildResponse(user, token);
	}

	// ── Login ─────────────────────────────────────────────────────────────────

	public AuthResponse login(LoginRequest request) {
		// Delegates to DaoAuthenticationProvider → CustomUserDetailsService

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		String token = jwtTokenProvider.generateToken(authentication);
		User user = (User) authentication.getPrincipal();
		return buildResponse(user, token);
	}

	// ── Helpers ───────────────────────────────────────────────────────────────

	private AuthResponse buildResponse(User user, String token) {
		return AuthResponse.builder().accessToken(token).userId(user.getId()).username(user.getUsername())
				.email(user.getEmail()).role(user.getRole()).build();
	}
}
