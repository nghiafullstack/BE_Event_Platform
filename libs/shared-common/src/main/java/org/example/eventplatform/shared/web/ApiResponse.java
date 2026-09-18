package org.example.eventplatform.shared.web;

/**
 * The one response envelope every endpoint in every service returns —
 * ResponseWrappingAdvice applies this automatically, so controllers keep
 * returning their DTO/Page/String/null as before and never build this by
 * hand except inside GlobalExceptionHandler.
 */
public record ApiResponse<T>(boolean success, String code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", null, data);
    }

    public static ApiResponse<Object> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
