package com.apexinnovators.dto;

/** Technology reference: {id,name,category,icon} (used inside projects and by admin). */
public record TechDto(
        Long id,
        String name,
        String category,
        String icon) {
}
