package com.crc.healthmetrics.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PageableValidator {

    public void validate(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("Page number must be greater than or equal to 0.");
        }
        if (pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0.");
        }
    }
}
