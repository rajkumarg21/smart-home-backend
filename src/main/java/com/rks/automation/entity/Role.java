package com.rks.automation.entity;

/**
 * Roles available in the system.
 * Spring Security expects role names prefixed with ROLE_ when using hasRole().
 */
public enum Role {
    ROLE_ADMIN,
    ROLE_USER,
    ROLE_MODERATOR
}
