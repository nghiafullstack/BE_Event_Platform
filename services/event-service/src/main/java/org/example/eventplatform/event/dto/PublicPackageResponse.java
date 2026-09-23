package org.example.eventplatform.event.dto;

import lombok.Builder;

import java.math.BigDecimal;

/** Gói show hiển thị cho khách, kèm tên đoàn để khách biết đang xem gói của ai. */
@Builder
public record PublicPackageResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Long troupeId,
        String troupeName,
        String troupeLogo
) {
}
