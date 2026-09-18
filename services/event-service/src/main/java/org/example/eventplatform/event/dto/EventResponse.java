package org.example.eventplatform.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventplatform.event.entity.EventStatus;
import org.example.eventplatform.event.entity.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String name;
    private EventType type;
    private String typeDisplayName;
    private EventStatus status;
    private String statusDisplayName;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private Long customerId;
    private Long tenantId;
    private LocalTime concentrateTime;
    private String concentrateLocation;
    private BigDecimal totalAmount;
    private BigDecimal platformFee;
    private LocalDateTime createdAt;
}
