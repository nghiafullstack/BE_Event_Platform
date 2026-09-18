package org.example.eventplatform.shared.exception;

import jakarta.persistence.EntityNotFoundException;
import org.example.eventplatform.shared.web.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Spring Boot's server.error.include-message property does not surface the
 * exception message on this stack (verified: still absent even passed as a
 * command-line override), so business errors are made visible here instead —
 * required for solo dev self-testing without a tester.
 *
 * Every error response is an ApiResponse with success=false and a stable
 * "code" a client can branch on: ApiException (thrown deliberately, one code
 * per known business case — see its javadoc) takes priority; the generic
 * handlers below are the fallback for exceptions nobody has migrated to
 * ApiException yet, or that will never need a specific code.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex) {
        return response(ex.getStatus(), ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        return response(HttpStatus.FORBIDDEN, "FORBIDDEN", "Bạn không có quyền thực hiện hành động này!");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(EntityNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(IllegalStateException ex) {
        return response(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        return response(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    private ResponseEntity<ApiResponse<Object>> response(HttpStatus status, String code, String message) {
        return new ResponseEntity<>(ApiResponse.error(code, message), status);
    }
}
