package org.example.eventplatform.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceCategoryRequest {

    @NotBlank(message = "Tên loại dịch vụ không được để trống")
    private String name;

    @NotBlank(message = "Code không được để trống")
    private String code;

    private String description;
}
