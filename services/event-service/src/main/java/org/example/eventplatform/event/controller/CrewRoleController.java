package org.example.eventplatform.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.event.dto.CrewRoleRequest;
import org.example.eventplatform.event.dto.CrewRoleResponse;
import org.example.eventplatform.event.service.CrewRoleService;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant/crew-roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CrewRoleController {

    private final CrewRoleService crewRoleService;

    @GetMapping
    public ResponseEntity<List<CrewRoleResponse>> list(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(crewRoleService.listForTenant(principal.tenantId()));
    }

    @PostMapping
    public ResponseEntity<CrewRoleResponse> create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody CrewRoleRequest request) {
        return new ResponseEntity<>(crewRoleService.create(principal.tenantId(), request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrewRoleResponse> update(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CrewRoleRequest request) {
        return ResponseEntity.ok(crewRoleService.update(principal.tenantId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable Long id) {
        crewRoleService.delete(principal.tenantId(), id);
        return ResponseEntity.noContent().build();
    }
}
