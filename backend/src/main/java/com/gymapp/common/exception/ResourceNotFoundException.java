package com.gymapp.common.exception;

import org.springframework.http.HttpStatus;

// ─── 404 Not Found ───────────────────────────────────────────────────────────

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.NOT_FOUND);
    }
}
