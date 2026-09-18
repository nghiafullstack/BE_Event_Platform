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

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<CustomerResponse> update(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, principal.tenantId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable Long id) {
        customerService.deleteCustomer(id, principal.tenantId());
        return ResponseEntity.noContent().build();
    }
}
