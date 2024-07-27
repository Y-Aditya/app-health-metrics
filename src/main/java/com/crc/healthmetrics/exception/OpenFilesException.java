package com.crc.healthmetrics.exception;

import org.springframework.http.HttpStatus;

public class OpenFilesException extends RuntimeException {
    private final HttpStatus status;

    public OpenFilesException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
