package org.example.eventplatform.event.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShowPackageRequest {

    @NotBlank(message = "Tên gói không được để trống")
    private String name;

    private String description;

    @DecimalMin(value = "0.0")
    private BigDecimal price;

    private Boolean active;
}
