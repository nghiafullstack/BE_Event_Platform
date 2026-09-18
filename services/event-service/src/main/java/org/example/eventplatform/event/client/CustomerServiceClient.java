package org.example.eventplatform.event.client;

import lombok.extern.slf4j.Slf4j;
import org.example.eventplatform.shared.client.InternalRestClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@Slf4j
public class CustomerServiceClient {

    private final RestClient restClient;

    public CustomerServiceClient(@Value("${customer-service.base-url}") String baseUrl,
                                  @Value("${internal.service-token:}") String internalToken) {
        this.restClient = InternalRestClients.create(baseUrl, internalToken);
    }

    public CustomerSummary findCustomer(Long customerId) {
        if (customerId == null) {
            return null;
        }
        try {
            return restClient.get()
                    .uri("/api/internal/customers/{id}", customerId)
                    .retrieve()
                    .body(CustomerSummary.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return null;
            }
            log.error("Could not fetch customer {}", customerId, ex);
            return null;
        } catch (Exception ex) {
            log.error("Could not fetch customer {}", customerId, ex);
            return null;
        }
    }

    /**
     * Strict lookup for write paths — throws when the customer is missing or the
     * remote call fails, so we never persist a dangling customerId.
     */
    public CustomerSummary requireCustomer(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId không được để trống");
        }
        try {
            return restClient.get()
                    .uri("/api/internal/customers/{id}", customerId)
                    .retrieve()
                    .body(CustomerSummary.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + customerId);
            }
            throw new IllegalStateException("Không gọi được customer-service để kiểm tra khách hàng", ex);
        }
    }

    public record CustomerSummary(Long id, Long tenantId, String fullName, String phone, String email) {
    }
}
