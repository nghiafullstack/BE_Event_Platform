package org.example.eventplatform.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.event.dto.ShowPackageRequest;
import org.example.eventplatform.event.dto.ShowPackageResponse;
import org.example.eventplatform.event.service.ShowPackageService;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** GET is open to any tenant member (needed to pick a package when self-creating a show); writes stay ADMIN-only. */
@RestController
@RequestMapping("/api/tenant/show-packages")
@RequiredArgsConstructor
public class ShowPackageController {

    private final ShowPackageService showPackageService;

    @GetMapping
    public ResponseEntity<List<ShowPackageResponse>> list(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(showPackageService.listForTenant(principal.tenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowPackageResponse> create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody ShowPackageRequest request) {
        return new ResponseEntity<>(showPackageService.create(principal.tenantId(), request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowPackageResponse> update(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ShowPackageRequest request) {
        return ResponseEntity.ok(showPackageService.update(principal.tenantId(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable Long id) {
        showPackageService.delete(principal.tenantId(), id);
        return ResponseEntity.noContent().build();
    }
}
