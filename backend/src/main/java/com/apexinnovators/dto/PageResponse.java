package com.apexinnovators.dto;

import java.util.List;

/**
 * Contract page envelope: {content, page, size, totalElements, totalPages}.
 * (Spring's own Page serializes the zero-based index as "number"; the contract
 * pins the field name "page", so we map explicitly.)
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
