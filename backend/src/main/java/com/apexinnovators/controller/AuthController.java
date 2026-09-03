package com.apexinnovators.controller;

import com.apexinnovators.dto.AuthResponse;
import com.apexinnovators.dto.LoginRequest;
import com.apexinnovators.dto.RefreshResponse;
import com.apexinnovators.dto.PasswordChangeRequest;
import com.apexinnovators.dto.ProfileResponse;
import com.apexinnovators.dto.ProfileUpdateRequest;
import com.apexinnovators.dto.RefreshTokenRequest;
import com.apexinnovators.dto.RegisterRequest;
import com.apexinnovators.dto.UserDto;
import com.apexinnovators.security.UserPrincipal;
import com.apexinnovators.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register, login, token refresh and current-user lookup")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new MEMBER account (status ACTIVE, empty profile row auto-created)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with email and password")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a refresh token (type=refresh JWT) for a new access token")
    public RefreshResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Current authenticated user")
    public UserDto me(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.me(principal.getId());
    }

    @GetMapping("/profile")
    @Operation(summary = "Signed-in member's account plus public profile")
    public ProfileResponse profile(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.profile(principal.getId());
    }

    @PutMapping("/profile")
    @Operation(summary = "Update own display name and profile fields (null fields unchanged)")
    public ProfileResponse updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                         @Valid @RequestBody ProfileUpdateRequest request) {
        return authService.updateProfile(principal, request);
    }

    @PutMapping("/password")
    @Operation(summary = "Change own password (verifies the current password first)")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                               @Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(principal, request);
        return ResponseEntity.noContent().build();
    }
}
