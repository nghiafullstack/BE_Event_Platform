package org.example.eventplatform.notification.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.eventplatform.notification.service.FcmTokenService;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    // userId comes from the JWT, never the request body — the old monolith trusted
    // a client-supplied userId here, which let anyone register a token for anyone.
    @PostMapping("/register")
    public ResponseEntity<Void> registerToken(@AuthenticationPrincipal JwtPrincipal principal, @RequestBody TokenRequest request) {
        fcmTokenService.registerToken(principal.userId(), request.getToken());
        return ResponseEntity.ok().build();
    }

    @Getter
    @Setter
    public static class TokenRequest {
        @NotBlank
        private String token;
    }
}
