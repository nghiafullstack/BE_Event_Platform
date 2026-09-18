package org.example.eventplatform.catalog.dto;

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
public class VendorProfileResponse {
    private Long id;
    private Long tenantId;
    private Long serviceCategoryId;
    private String serviceCategoryName;
    private String businessName;
    private String description;
    private String logoUrl;
    private String address;
    private boolean active;
}
