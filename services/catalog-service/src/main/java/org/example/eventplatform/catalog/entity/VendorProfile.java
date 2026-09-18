package org.example.eventplatform.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.eventplatform.shared.entity.BaseEntity;

/**
 * How a tenant references a ServiceCategory: not a JPA relation (identity-service
 * owns Tenant, catalog-service owns ServiceCategory — different databases), just
 * the pair of foreign ids here. One profile per tenant for now — a tenant sells
 * as one vendor type; multi-category vendors are a later extension.
 */
@Entity
@Table(name = "vendor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

    @Column(name = "service_category_id", nullable = false)
    private Long serviceCategoryId;

    @Column(nullable = false)
    private String businessName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String logoUrl;
    private String address;

    @Builder.Default
    private boolean active = true;
}
