package com.apexinnovators.dto;

import com.apexinnovators.entity.MessageStatus;
import java.time.LocalDateTime;

/** Admin inbox message item: {id,name,email,subject,message,status,createdAt}. */
public record AdminMessageDto(
        Long id,
        String name,
        String email,
        String subject,
        String message,
        MessageStatus status,
        LocalDateTime createdAt) {
}
