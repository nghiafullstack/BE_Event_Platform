package org.example.eventplatform.event.service;

import lombok.RequiredArgsConstructor;
import org.example.eventplatform.event.client.IdentityServiceClient;
import org.example.eventplatform.event.dto.PublicPackageResponse;
import org.example.eventplatform.event.dto.PublicTroupeResponse;
import org.example.eventplatform.event.entity.ShowPackage;
import org.example.eventplatform.event.repository.ShowPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sàn công khai cho khách thuê sự kiện: duyệt đoàn và gói show của mọi đoàn.
 * Không nhận JwtPrincipal vì khách vãng lai chưa đăng nhập cũng xem được, nên
 * mọi truy vấn ở đây chỉ được chạm vào dữ liệu đã đánh dấu công khai
 * (đoàn đang hoạt động, gói đang mở bán).
 */
@Service
@RequiredArgsConstructor
public class PublicCatalogService {

    private final ShowPackageRepository showPackageRepository;
    private final IdentityServiceClient identityServiceClient;

    @Transactional(readOnly = true)
    public List<PublicTroupeResponse> listTroupes() {
        Map<Long, List<ShowPackage>> packagesByTenant = activePackagesByTenant();
        return identityServiceClient.findPublicTenants().stream()
                .map(tenant -> toTroupe(tenant, packagesByTenant.getOrDefault(tenant.id(), List.of()), false))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicTroupeResponse getTroupe(Long troupeId) {
        IdentityServiceClient.PublicTenant tenant = identityServiceClient.findPublicTenants().stream()
                .filter(t -> t.id().equals(troupeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đoàn này"));
        List<ShowPackage> packages = showPackageRepository.findByTenantId(troupeId).stream()
                .filter(ShowPackage::isActive)
                .toList();
        return toTroupe(tenant, packages, true);
    }

    @Transactional(readOnly = true)
    public List<PublicPackageResponse> listPackages() {
        Map<Long, IdentityServiceClient.PublicTenant> tenants = identityServiceClient.findPublicTenants().stream()
                .collect(Collectors.toMap(IdentityServiceClient.PublicTenant::id, t -> t, (a, b) -> a));
        // Bỏ gói của đoàn đã ngừng hoạt động — findPublicTenants chỉ trả đoàn đang hoạt động.
        return showPackageRepository.findByActiveTrue().stream()
                .filter(pkg -> tenants.containsKey(pkg.getTenantId()))
                .map(pkg -> toPackage(pkg, tenants.get(pkg.getTenantId())))
                .toList();
    }

    private Map<Long, List<ShowPackage>> activePackagesByTenant() {
        return showPackageRepository.findByActiveTrue().stream()
                .collect(Collectors.groupingBy(ShowPackage::getTenantId));
    }

    private PublicTroupeResponse toTroupe(IdentityServiceClient.PublicTenant tenant,
                                          List<ShowPackage> packages,
                                          boolean includePackages) {
        BigDecimal fromPrice = packages.stream()
                .map(ShowPackage::getPrice)
                .filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        return PublicTroupeResponse.builder()
                .id(tenant.id())
                .name(tenant.name())
                .domain(tenant.domain())
                .logo(tenant.logo())
                .category(tenant.category())
                .primaryColorHex(tenant.primaryColorHex())
                .accentColorHex(tenant.accentColorHex())
                .packageCount(packages.size())
                .fromPrice(fromPrice)
                .packages(includePackages ? packages.stream().map(pkg -> toPackage(pkg, tenant)).toList() : null)
                .build();
    }

    private PublicPackageResponse toPackage(ShowPackage pkg, IdentityServiceClient.PublicTenant tenant) {
        return PublicPackageResponse.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .price(pkg.getPrice())
                .troupeId(tenant != null ? tenant.id() : pkg.getTenantId())
                .troupeName(tenant != null ? tenant.name() : null)
                .troupeLogo(tenant != null ? tenant.logo() : null)
                .build();
    }
}
