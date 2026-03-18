package com.gymapp.common.exception;

import org.springframework.http.HttpStatus;

// ─── 403 Forbidden ───────────────────────────────────────────────────────────

public class ForbiddenException extends DomainException {
    public ForbiddenException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.FORBIDDEN);
    }
}
