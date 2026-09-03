package com.apexinnovators.dto;

/** Contact submission response: {name,email,subject,message}. */
public record ContactDto(
        String name,
        String email,
        String subject,
        String message) {
}
