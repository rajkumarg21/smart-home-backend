package com.rks.automation.user.service.impl;

import com.rks.automation.dto.UserProfileDto;
import com.rks.automation.entity.User;
import com.rks.automation.repository.UserRepository;
import com.rks.automation.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link UserService}.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	

	@Override
	@Transactional(readOnly = true)
	public UserProfileDto getProfile(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		return UserProfileDto.from(user);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserProfileDto> getAllUsers() {
		return userRepository.findAll().stream().map(UserProfileDto::from).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public UserProfileDto getUserById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
		return UserProfileDto.from(user);
	}

	@Override
	@Transactional
	public void deleteUser(Long id) {
		if (!userRepository.existsById(id)) {
			throw new UsernameNotFoundException("User not found with id: " + id);
		}
		userRepository.deleteById(id);
	}
}
