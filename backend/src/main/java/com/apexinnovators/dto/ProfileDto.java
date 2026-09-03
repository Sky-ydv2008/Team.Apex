package com.apexinnovators.dto;

/** Profile fields exposed to admins. */
public record ProfileDto(
        String headline,
        String bio,
        String photoUrl,
        String github,
        String linkedin) {
}
