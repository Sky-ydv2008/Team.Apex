package com.apexinnovators.dto;

/** Linked project inside a hackathon: {id,title,slug}. */
public record HackathonProjectLinkDto(
        Long id,
        String title,
        String slug) {
}
