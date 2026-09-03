package com.apexinnovators.dto;

import com.apexinnovators.entity.EventMode;
import java.time.LocalDateTime;

/** Event payload (contract field names): {id,title,date,mode,location,description}. */
public record EventDto(
        Long id,
        String title,
        LocalDateTime date,
        EventMode mode,
        String location,
        String description) {
}
