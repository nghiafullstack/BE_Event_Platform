package org.example.eventplatform.shared.security;

import java.util.List;

/**
 * Claims extracted from a validated JWT — deliberately independent of any
 * service's own User entity so every service can authenticate a request
 * without calling back into identity-service.
 */
public record JwtPrincipal(Long userId, String username, Long tenantId, List<String> authorities) {
}
