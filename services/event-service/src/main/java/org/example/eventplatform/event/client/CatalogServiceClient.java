package org.example.eventplatform.event.client;

import lombok.extern.slf4j.Slf4j;
import org.example.eventplatform.shared.client.InternalRestClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@Slf4j
public class CatalogServiceClient {

    private final RestClient restClient;

    public CatalogServiceClient(@Value("${catalog-service.base-url}") String baseUrl,
                                 @Value("${internal.service-token:}") String internalToken) {
        this.restClient = InternalRestClients.create(baseUrl, internalToken);
    }

    public VendorProfileSummary findVendorByTenant(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        try {
            return restClient.get()
                    .uri("/api/internal/vendor-profiles/by-tenant/{tenantId}", tenantId)
                    .retrieve()
                    .body(VendorProfileSummary.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return null;
            }
            log.error("Could not fetch vendor profile for tenant {}", tenantId, ex);
            return null;
        } catch (Exception ex) {
            log.error("Could not fetch vendor profile for tenant {}", tenantId, ex);
            return null;
        }
    }

    public record VendorProfileSummary(
            Long id,
            Long tenantId,
            Long serviceCategoryId,
            String serviceCategoryName,
            String businessName,
            boolean active
    ) {
    }
}
