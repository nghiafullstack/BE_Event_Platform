package org.example.eventplatform.shared.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.example.eventplatform.shared.web.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    /**
     * Bean Validation failures on a @Valid @RequestBody DTO (@NotBlank,
     * @Size, @Email, ...) — without this handler Spring resolves them via
     * its default error machinery before ResponseWrappingAdvice ever runs,
     * so the client gets an unwrapped, message-less body and the actual
     * reason (e.g. "password must be at least 6 characters") is only ever
     * visible in the server log.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Dữ liệu gửi lên không hợp lệ");
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    /** Same as above, for @Validated path/query params instead of a @RequestBody DTO. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse("Dữ liệu gửi lên không hợp lệ");
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return response(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", "Thiếu tham số bắt buộc: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return response(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", "Tham số \"" + ex.getName() + "\" không đúng định dạng");
    }

    /** Malformed JSON body, or a value that doesn't match its target type/enum. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        return response(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Dữ liệu gửi lên không đúng định dạng");
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
