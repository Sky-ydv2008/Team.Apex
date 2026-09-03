package com.apexinnovators.dto;

/** Login/register response: {token, refreshToken, user}. */
public record AuthResponse(
        String token,
        String refreshToken,
        UserDto user) {
}
