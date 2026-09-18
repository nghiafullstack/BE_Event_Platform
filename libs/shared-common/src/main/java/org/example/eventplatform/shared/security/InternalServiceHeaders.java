package org.example.eventplatform.shared.security;

/**
 * Shared header for synchronous service-to-service calls on {@code /api/internal/**}.
 * Public clients never see this — the gateway does not route {@code /api/internal/**}.
 */
public final class InternalServiceHeaders {

    public static final String TOKEN = "X-Internal-Token";

    private InternalServiceHeaders() {
    }
}
