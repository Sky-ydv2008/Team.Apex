package com.apexinnovators.service;

import com.apexinnovators.audit.AuditService;
import com.apexinnovators.dto.AuthResponse;
import com.apexinnovators.dto.LoginRequest;
import com.apexinnovators.dto.RefreshResponse;
import com.apexinnovators.dto.RefreshTokenRequest;
import com.apexinnovators.dto.PasswordChangeRequest;
import com.apexinnovators.dto.ProfileResponse;
import com.apexinnovators.dto.ProfileUpdateRequest;
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
import com.apexinnovators.security.UserPrincipal;
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
    private final AuditService auditService;

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

    /** Signed-in member views the account plus their public profile. */
    @Transactional(readOnly = true)
    public ProfileResponse profile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account not found"));
        return toProfileResponse(user);
    }

    /** Members control their own display profile and display name. */
    @Transactional
    public ProfileResponse updateProfile(UserPrincipal actor, ProfileUpdateRequest request) {
        User user = userRepository.findById(actor.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account not found"));
        if (request.name() != null && !request.name().trim().equals(user.getName())) {
            user.setName(request.name().trim());
            userRepository.save(user);
        }
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            profile = new Profile();
            profile.setUserId(user.getId());
        }
        boolean touched = false;
        if (request.headline() != null && !request.headline().equals(profile.getHeadline())) {
            profile.setHeadline(request.headline()); touched = true;
        }
        if (request.bio() != null && !request.bio().equals(profile.getBio())) {
            profile.setBio(request.bio()); touched = true;
        }
        if (request.github() != null && !request.github().equals(profile.getGithub())) {
            profile.setGithub(request.github()); touched = true;
        }
        if (request.linkedin() != null && !request.linkedin().equals(profile.getLinkedin())) {
            profile.setLinkedin(request.linkedin()); touched = true;
        }
        if (request.photoUrl() != null && !request.photoUrl().equals(profile.getPhotoUrl())) {
            profile.setPhotoUrl(request.photoUrl()); touched = true;
        }
        if (touched || profile.getId() == null) {
            profileRepository.save(profile);
        }
        auditService.record(actor.getId(), "UPDATE", "Profile", user.getId(),
                "Updated own profile (user #" + actor.getId() + ")");
        return toProfileResponse(user);
    }

    /** Members change their own password after confirming the current one. */
    @Transactional
    public void changePassword(UserPrincipal actor, PasswordChangeRequest request) {
        User user = userRepository.findById(actor.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        auditService.record(actor.getId(), "UPDATE", "Password", user.getId(),
                "Changed own password (user #" + actor.getId() + ")");
    }

    private ProfileResponse toProfileResponse(User user) {
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        return new ProfileResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus(),
                profile == null ? null : profile.getHeadline(),
                profile == null ? null : profile.getBio(),
                profile == null ? null : profile.getGithub(),
                profile == null ? null : profile.getLinkedin(),
                profile == null ? null : profile.getPhotoUrl());
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
