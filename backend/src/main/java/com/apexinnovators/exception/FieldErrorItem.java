package com.apexinnovators.exception;

/** One failing field of a validation error. */
public record FieldErrorItem(String field, String message) {
}
