package org.example.eventplatform.notification.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.eventplatform.notification.service.FcmTokenService;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerToken(@AuthenticationPrincipal JwtPrincipal principal,
                                               @Valid @RequestBody TokenRequest request) {
        fcmTokenService.registerToken(principal.userId(), request.getToken());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/register")
    public ResponseEntity<Void> unregisterToken(@AuthenticationPrincipal JwtPrincipal principal,
                                                 @Valid @RequestBody TokenRequest request) {
        fcmTokenService.unregisterToken(principal.userId(), request.getToken());
        return ResponseEntity.noContent().build();
    }

    @Getter
    @Setter
    public static class TokenRequest {
        @NotBlank
        private String token;
    }
}
