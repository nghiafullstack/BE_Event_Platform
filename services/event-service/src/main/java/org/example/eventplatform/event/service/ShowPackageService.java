package org.example.eventplatform.event.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.event.dto.ShowPackageRequest;
import org.example.eventplatform.event.dto.ShowPackageResponse;
import org.example.eventplatform.event.entity.ShowPackage;
import org.example.eventplatform.event.repository.ShowPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowPackageService {

    private final ShowPackageRepository showPackageRepository;

    @Transactional(readOnly = true)
    public List<ShowPackageResponse> listForTenant(Long tenantId) {
        return showPackageRepository.findByTenantId(tenantId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ShowPackageResponse create(Long tenantId, ShowPackageRequest request) {
        ShowPackage pkg = ShowPackage.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .active(request.getActive() == null || request.getActive())
                .build();
        return toResponse(showPackageRepository.save(pkg));
    }

    @Transactional
    public ShowPackageResponse update(Long tenantId, Long id, ShowPackageRequest request) {
        ShowPackage pkg = getOwnedOrThrow(tenantId, id);
        pkg.setName(request.getName());
        pkg.setDescription(request.getDescription());
        pkg.setPrice(request.getPrice());
        if (request.getActive() != null) {
            pkg.setActive(request.getActive());
        }
        return toResponse(showPackageRepository.save(pkg));
    }

    @Transactional
    public void delete(Long tenantId, Long id) {
        showPackageRepository.delete(getOwnedOrThrow(tenantId, id));
    }

    ShowPackage getOwnedOrThrow(Long tenantId, Long id) {
        return showPackageRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy gói show với ID: " + id));
    }

    private ShowPackageResponse toResponse(ShowPackage pkg) {
        return ShowPackageResponse.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .price(pkg.getPrice())
                .active(pkg.isActive())
                .build();
    }
}
