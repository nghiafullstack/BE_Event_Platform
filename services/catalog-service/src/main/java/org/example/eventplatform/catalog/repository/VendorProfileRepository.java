package org.example.eventplatform.catalog.repository;

import org.example.eventplatform.catalog.entity.VendorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, Long> {
    Optional<VendorProfile> findByTenantId(Long tenantId);

    boolean existsByTenantId(Long tenantId);

    Page<VendorProfile> findByServiceCategoryIdAndActiveTrue(Long serviceCategoryId, Pageable pageable);

    Page<VendorProfile> findByActiveTrue(Pageable pageable);
}
