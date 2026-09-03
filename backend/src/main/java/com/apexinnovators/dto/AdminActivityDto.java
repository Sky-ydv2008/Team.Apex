package com.apexinnovators.dto;

import java.time.LocalDateTime;

/** One recent audit entry for the admin overview: {actor,action,entity,detail,createdAt}. */
public record AdminActivityDto(
        String actor,
        String action,
        String entity,
        String detail,
        LocalDateTime createdAt) {
}
