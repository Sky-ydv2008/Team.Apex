package com.apexinnovators.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Contract error envelope: {status, message, timestamp, path}. Validation
 * failures additionally carry {@code errors} ({field, message} pairs).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String message,
        String timestamp,
        String path,
        List<FieldErrorItem> errors) {
}
