package org.example.eventplatform.customer.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.customer.dto.CustomerRequest;
import org.example.eventplatform.customer.dto.CustomerResponse;
import org.example.eventplatform.customer.entity.Customer;
import org.example.eventplatform.customer.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request, Long tenantId) {
        if (customerRepository.existsByPhoneAndTenantId(request.getPhone(), tenantId)) {
            throw new RuntimeException("Số điện thoại này đã tồn tại trong hệ thống của bạn");
        }

        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .type(request.getType())
                .note(request.getNote())
                .assignedToUserId(request.getAssignedToUserId())
                .tenantId(tenantId)
                .active(true)
                .build();

        return toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getCustomers(Long tenantId, String keyword, Pageable pageable) {
        return customerRepository.searchCustomers(tenantId, keyword, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id, Long tenantId) {
        return toResponse(getOrThrow(id, tenantId));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, Long tenantId, CustomerRequest request) {
        Customer existing = getOrThrow(id, tenantId);

        if (!existing.getPhone().equals(request.getPhone())
                && customerRepository.existsByPhoneAndTenantId(request.getPhone(), tenantId)) {
            throw new RuntimeException("Số điện thoại mới đã bị trùng trong hệ thống");
        }

        existing.setFullName(request.getFullName());
        existing.setPhone(request.getPhone());
        existing.setEmail(request.getEmail());
        existing.setAddress(request.getAddress());
        existing.setType(request.getType());
        existing.setNote(request.getNote());
        existing.setAssignedToUserId(request.getAssignedToUserId());

        return toResponse(customerRepository.save(existing));
    }

    @Transactional
    public void deleteCustomer(Long id, Long tenantId) {
        Customer customer = getOrThrow(id, tenantId);
        customerRepository.delete(customer);
    }

    private Customer getOrThrow(Long id, Long tenantId) {
        return customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng"));
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .type(customer.getType())
                .note(customer.getNote())
                .active(customer.isActive())
                .assignedToUserId(customer.getAssignedToUserId())
                .tenantId(customer.getTenantId())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
