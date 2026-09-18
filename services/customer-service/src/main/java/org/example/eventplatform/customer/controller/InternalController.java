package org.example.eventplatform.customer.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.customer.dto.internal.CustomerSummaryResponse;
import org.example.eventplatform.customer.entity.Customer;
import org.example.eventplatform.customer.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service only — guarded by {@code X-Internal-Token}.
 */
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final CustomerRepository customerRepository;

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerSummaryResponse> getCustomer(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với ID: " + id));
        return ResponseEntity.ok(CustomerSummaryResponse.builder()
                .id(customer.getId())
                .tenantId(customer.getTenantId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .build());
    }
}
