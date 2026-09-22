package org.example.eventplatform.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.customer.dto.CustomerRequest;
import org.example.eventplatform.customer.dto.CustomerResponse;
import org.example.eventplatform.customer.service.CustomerService;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Read + create are open to any authenticated tenant member (a TN_MEMBER
 * self-booking a show needs to search for and add customers); update/delete
 * stay ADMIN-only. Every method still scopes by principal.tenantId(), so a
 * member never sees or touches another tenant's customers regardless.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getCustomers(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(customerService.getCustomers(principal.tenantId(), keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id, principal.tenantId()));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request, principal.tenantId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> update(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, principal.tenantId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable Long id) {
        customerService.deleteCustomer(id, principal.tenantId());
        return ResponseEntity.noContent().build();
    }
}
