package org.example.eventplatform.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorProfileRequest {

    @NotNull(message = "serviceCategoryId không được để trống")
    private Long serviceCategoryId;

    @NotBlank(message = "Tên hiển thị không được để trống")
    private String businessName;

    private String description;
    private String logoUrl;
    private String address;
}
