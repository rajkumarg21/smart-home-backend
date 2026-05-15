package com.rks.automation.service;

import java.util.List;

import com.rks.automation.dto.UserProfileDto;

/**
 * Contract for user profile and admin operations.
 */
public interface UserService {

    /** Return the profile of the currently authenticated user. */
    UserProfileDto getProfile(String username);

    /** Admin: list all users. */
    List<UserProfileDto> getAllUsers();

    /** Admin: get any user by id. */
    UserProfileDto getUserById(Long id);

    /** Admin: delete a user. */
    void deleteUser(Long id);
}
