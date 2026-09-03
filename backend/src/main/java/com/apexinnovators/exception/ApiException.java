package com.apexinnovators.exception;

import org.springframework.http.HttpStatus;

/** Domain exception carrying the HTTP status the response should use. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
