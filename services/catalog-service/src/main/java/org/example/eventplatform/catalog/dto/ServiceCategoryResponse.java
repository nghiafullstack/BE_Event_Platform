package org.example.eventplatform.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private boolean active;
}
