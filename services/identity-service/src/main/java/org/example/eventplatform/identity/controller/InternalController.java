package org.example.eventplatform.identity.controller;

import lombok.RequiredArgsConstructor;
import org.example.eventplatform.identity.dto.internal.AdminContactResponse;
import org.example.eventplatform.identity.entity.User;
import org.example.eventplatform.identity.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Service-to-service only — trusted network boundary, no gateway/mTLS between
 * services yet (that lands with Phase 6). Never route this through anything
 * a browser or the Flutter app can reach directly.
 */
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final UserRepository userRepository;

    @GetMapping("/tenants/{tenantId}/admins")
    public ResponseEntity<List<AdminContactResponse>> getTenantAdmins(@PathVariable Long tenantId) {
        List<AdminContactResponse> admins = userRepository.findByTenantIdAndRoleName(tenantId, "ADMIN").stream()
                .map(this::toContact)
                .toList();
        return ResponseEntity.ok(admins);
    }

    private AdminContactResponse toContact(User user) {
        return AdminContactResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
