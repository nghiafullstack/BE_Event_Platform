package org.example.eventplatform.identity.dto.user;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class UpdateCommissionRateRequest {
    @NotNull(message = "% hoa hồng không được để trống")
    @DecimalMin(value = "0.0", message = "% hoa hồng không được âm")
    @DecimalMax(value = "100.0", message = "% hoa hồng không được vượt quá 100")
    private BigDecimal commissionRate;
}
