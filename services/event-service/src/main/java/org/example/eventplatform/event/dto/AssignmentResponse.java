package org.example.eventplatform.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private Long id;
    private Long eventId;
    private String eventName;
    private LocalDate eventDate;
    private String location;

    private Long userId;
    private String userFullName;
    private String position;
    private String status;
    private String note;

    private LocalTime actualConcentrateAt;
    private LocalTime checkinAt;
    private LocalTime checkoutAt;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime concentrateTime;
    private String concentrateLocation;

    private List<Teammate> teammates;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Teammate {
        private Long userId;
        private String fullName;
        private String position;
        private String status;
    }
}
