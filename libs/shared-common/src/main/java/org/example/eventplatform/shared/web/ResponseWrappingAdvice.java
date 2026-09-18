package org.example.eventplatform.shared.web;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Wraps every controller response in ApiResponse without touching a single
 * controller — nothing here is written by hand except inside
 * GlobalExceptionHandler, which builds ApiResponse.error(...) directly (this
 * advice recognizes that and passes it through unwrapped).
 */
@RestControllerAdvice
public class ResponseWrappingAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ApiResponse<?>) {
            return body;
        }
        // A null body (e.g. ResponseEntity.noContent().build()) means the method
        // deliberately wants no body written — a 204 must stay bodyless.
        if (body == null) {
            return null;
        }
        return ApiResponse.success(body);
    }
}
