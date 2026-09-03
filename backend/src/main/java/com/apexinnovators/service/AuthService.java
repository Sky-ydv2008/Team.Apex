package com.apexinnovators.service;

import com.apexinnovators.dto.AuthResponse;
import com.apexinnovators.dto.LoginRequest;
import com.apexinnovators.dto.RefreshResponse;
import com.apexinnovators.dto.RefreshTokenRequest;
import com.apexinnovators.dto.RegisterRequest;
import com.apexinnovators.dto.UserDto;
import com.apexinnovators.entity.Profile;
import com.apexinnovators.entity.Role;
import com.apexinnovators.entity.User;
import com.apexinnovators.entity.UserStatus;
import com.apexinnovators.exception.ApiException;
import com.apexinnovators.repository.ProfileRepository;
import com.apexinnovators.repository.UserRepository;
import com.apexinnovators.security.JwtService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Creates a MEMBER account (ACTIVE) plus its empty profile row, then issues tokens. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.MEMBER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        Profile profile = new Profile();
        profile.setUserId(user.getId());
        profileRepository.save(profile);

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (DisabledException ex) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is suspended");
        } catch (BadCredentialsException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public RefreshResponse refresh(RefreshTokenRequest request) {
        if (!jwtService.isValid(request.refreshToken(), JwtService.TYPE_REFRESH)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
        String email = jwtService.parse(request.refreshToken()).getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is suspended");
        }
        return new RefreshResponse(jwtService.generateAccessToken(user.getEmail()));
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account not found"));
        return toUserDto(user);
    }

    public UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        return new AuthResponse(accessToken, refreshToken, toUserDto(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
