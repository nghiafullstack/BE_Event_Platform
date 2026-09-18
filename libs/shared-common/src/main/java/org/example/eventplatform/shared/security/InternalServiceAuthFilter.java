package org.example.eventplatform.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Guards {@code /api/internal/**}. Not a {@code @Component}: each service that
 * exposes internal endpoints registers it in its own SecurityConfig with the
 * shared {@code INTERNAL_SERVICE_TOKEN}.
 */
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    private final String expectedToken;

    public InternalServiceAuthFilter(String expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/internal");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (!StringUtils.hasText(expectedToken)) {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), "Internal service token is not configured");
            return;
        }
        String provided = request.getHeader(InternalServiceHeaders.TOKEN);
        if (!expectedToken.equals(provided)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or missing internal service token");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
