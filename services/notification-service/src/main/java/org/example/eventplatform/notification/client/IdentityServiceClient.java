package org.example.eventplatform.notification.client;

import lombok.extern.slf4j.Slf4j;
import org.example.eventplatform.shared.client.InternalRestClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Slf4j
public class IdentityServiceClient {

    private final RestClient restClient;

    public IdentityServiceClient(@Value("${identity-service.base-url}") String baseUrl,
                                  @Value("${internal.service-token:}") String internalToken) {
        this.restClient = InternalRestClients.create(baseUrl, internalToken);
    }

    public List<AdminContact> getTenantAdmins(Long tenantId) {
        try {
            AdminContact[] response = restClient.get()
                    .uri("/api/internal/tenants/{tenantId}/admins", tenantId)
                    .retrieve()
                    .body(AdminContact[].class);
            return response != null ? List.of(response) : List.of();
        } catch (Exception ex) {
            log.error("Could not fetch admins for tenant {}", tenantId, ex);
            return List.of();
        }
    }
}
