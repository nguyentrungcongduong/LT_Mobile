package com.gymapp.common.exception;

import org.springframework.http.HttpStatus;

// ─── 401 Unauthorized ────────────────────────────────────────────────────────

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.UNAUTHORIZED);
    }
}
