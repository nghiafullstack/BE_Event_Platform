package org.example.eventplatform.event.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class UpdateConcentrateRequest {
    private LocalTime concentrateTime;
    private String concentrateLocation;
}
