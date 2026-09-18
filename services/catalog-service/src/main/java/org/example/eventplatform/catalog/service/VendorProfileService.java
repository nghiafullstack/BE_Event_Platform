package org.example.eventplatform.catalog.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.catalog.dto.VendorProfileRequest;
import org.example.eventplatform.catalog.dto.VendorProfileResponse;
import org.example.eventplatform.catalog.entity.ServiceCategory;
import org.example.eventplatform.catalog.entity.VendorProfile;
import org.example.eventplatform.catalog.repository.ServiceCategoryRepository;
import org.example.eventplatform.catalog.repository.VendorProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VendorProfileService {

    private final VendorProfileRepository vendorProfileRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;

    @Transactional
    public VendorProfileResponse createOrUpdateMyProfile(Long tenantId, VendorProfileRequest request) {
        ServiceCategory category = serviceCategoryRepository.findById(request.getServiceCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy loại dịch vụ"));
        if (!category.isActive()) {
            throw new RuntimeException("Loại dịch vụ này đã ngừng hoạt động");
        }

        VendorProfile profile = vendorProfileRepository.findByTenantId(tenantId).orElseGet(VendorProfile::new);
        profile.setTenantId(tenantId);
        profile.setServiceCategoryId(request.getServiceCategoryId());
        profile.setBusinessName(request.getBusinessName());
        profile.setDescription(request.getDescription());
        profile.setLogoUrl(request.getLogoUrl());
        profile.setAddress(request.getAddress());
        if (profile.getId() == null) {
            profile.setActive(true);
        }

        return toResponse(vendorProfileRepository.save(profile), category);
    }

    @Transactional(readOnly = true)
    public VendorProfileResponse getMyProfile(Long tenantId) {
        VendorProfile profile = vendorProfileRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Bạn chưa tạo hồ sơ vendor"));
        return toResponse(profile, getCategory(profile.getServiceCategoryId()));
    }

    @Transactional(readOnly = true)
    public VendorProfileResponse getPublicProfile(Long id) {
        VendorProfile profile = vendorProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hồ sơ vendor"));
        return toResponse(profile, getCategory(profile.getServiceCategoryId()));
    }

    @Transactional(readOnly = true)
    public Page<VendorProfileResponse> listVendors(Long serviceCategoryId, Pageable pageable) {
        Page<VendorProfile> page = serviceCategoryId != null
                ? vendorProfileRepository.findByServiceCategoryIdAndActiveTrue(serviceCategoryId, pageable)
                : vendorProfileRepository.findByActiveTrue(pageable);
        return page.map(p -> toResponse(p, getCategory(p.getServiceCategoryId())));
    }

    private ServiceCategory getCategory(Long id) {
        return serviceCategoryRepository.findById(id).orElse(null);
    }

    private VendorProfileResponse toResponse(VendorProfile profile, ServiceCategory category) {
        return VendorProfileResponse.builder()
                .id(profile.getId())
                .tenantId(profile.getTenantId())
                .serviceCategoryId(profile.getServiceCategoryId())
                .serviceCategoryName(category != null ? category.getName() : null)
                .businessName(profile.getBusinessName())
                .description(profile.getDescription())
                .logoUrl(profile.getLogoUrl())
                .address(profile.getAddress())
                .active(profile.isActive())
                .build();
    }
}
