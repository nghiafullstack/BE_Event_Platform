package org.example.eventplatform.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowPackageResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean active;
}
