package com.crc.healthmetrics.exception;

import org.springframework.http.HttpStatus;

public class MetricsCaptureException extends RuntimeException {
    private final HttpStatus status;

    public MetricsCaptureException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
