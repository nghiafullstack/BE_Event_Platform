package org.example.eventplatform.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CustomerBookingRequest {

    @NotNull
    private Long tenantId;

    @NotNull
    private Long packageId;

    @NotNull
    private LocalDate eventDate;

    @NotNull
    private LocalTime startTime;

    @NotBlank
    private String location;

    private Double lat;

    private Double lng;

    /** Yêu cầu riêng từ khách — lưu vào Event.description. */
    private String note;
}
