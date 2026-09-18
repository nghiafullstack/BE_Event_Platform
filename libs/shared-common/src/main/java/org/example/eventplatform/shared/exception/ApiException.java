package org.example.eventplatform.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Throw this instead of a plain RuntimeException when the failure is a known
 * business case a client should be able to branch on — code is a stable,
 * machine-readable token (e.g. "CUSTOMER_PHONE_DUPLICATE"), independent of
 * the human-readable Vietnamese message, which may still change wording.
 * Each service defines its own codes (prefixed by domain) — no shared enum
 * to edit for every new business rule.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
