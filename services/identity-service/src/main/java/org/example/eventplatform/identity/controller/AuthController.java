package org.example.eventplatform.identity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.identity.dto.auth.AuthTokenResponse;
import org.example.eventplatform.identity.dto.auth.LoginRequest;
import org.example.eventplatform.identity.dto.auth.RefreshTokenRequest;
import org.example.eventplatform.identity.dto.auth.UserSummaryResponse;
import org.example.eventplatform.identity.service.AuthService;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryResponse> getMe(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(authService.getMe(principal.userId()));
    }
}
