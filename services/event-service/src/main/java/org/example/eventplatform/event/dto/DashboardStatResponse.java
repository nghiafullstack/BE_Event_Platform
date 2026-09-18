package org.example.eventplatform.event.dto;

public record DashboardStatResponse(
        long totalShows,
        long pendingShows,
        java.math.BigDecimal totalEarnings,
        String rank
) {
}
