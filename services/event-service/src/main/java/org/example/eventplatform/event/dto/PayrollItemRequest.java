package org.example.eventplatform.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PayrollItemRequest {
    @NotBlank(message = "label không được để trống")
    private String label;

    @NotNull(message = "amount không được để trống")
    private BigDecimal amount;
}
