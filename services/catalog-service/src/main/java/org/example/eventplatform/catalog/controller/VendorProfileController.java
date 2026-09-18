package org.example.eventplatform.catalog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.catalog.dto.VendorProfileRequest;
import org.example.eventplatform.catalog.dto.VendorProfileResponse;
import org.example.eventplatform.catalog.service.VendorProfileService;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class VendorProfileController {

    private final VendorProfileService vendorProfileService;

    // ===== Tenant admin quản lý hồ sơ vendor của chính mình =====

    @GetMapping("/api/tenant/vendor-profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VendorProfileResponse> getMyProfile(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(vendorProfileService.getMyProfile(principal.tenantId()));
    }

    @PutMapping("/api/tenant/vendor-profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VendorProfileResponse> upsertMyProfile(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody VendorProfileRequest request) {
        return ResponseEntity.ok(vendorProfileService.createOrUpdateMyProfile(principal.tenantId(), request));
    }

    // ===== Public — hồ sơ vendor công khai =====

    @GetMapping("/api/vendor-profiles")
    public ResponseEntity<Page<VendorProfileResponse>> listVendors(
            @RequestParam(required = false) Long serviceCategoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(vendorProfileService.listVendors(serviceCategoryId, pageable));
    }

    @GetMapping("/api/vendor-profiles/{id}")
    public ResponseEntity<VendorProfileResponse> getPublicProfile(@PathVariable Long id) {
        return ResponseEntity.ok(vendorProfileService.getPublicProfile(id));
    }
}
