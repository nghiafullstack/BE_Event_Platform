package org.example.eventplatform.catalog.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProfileSummaryResponse {
    private Long id;
    private Long tenantId;
    private Long serviceCategoryId;
    private String serviceCategoryName;
    private String businessName;
    private boolean active;
}
