package com.rks.automation.controller;

import com.rks.automation.common.ApiResponse;
import com.rks.automation.dto.AuthResponse;
import com.rks.automation.dto.LoginRequest;
import com.rks.automation.dto.RegisterRequest;
import com.rks.automation.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints — all public (no JWT required).
 *
 * POST /api/auth/register  → create account, returns JWT
 * POST /api/auth/login     → authenticate, returns JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
   
    /**
     * Register a new user.
     * Body: { "username", "email", "password", "fullName", "role" (optional) }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Login with username + password.
     * Body: { "username", "password" }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
