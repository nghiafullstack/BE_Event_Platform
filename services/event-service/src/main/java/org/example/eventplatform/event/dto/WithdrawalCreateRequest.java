package org.example.eventplatform.event.dto;

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
public class WithdrawalCreateRequest {

    @NotNull(message = "Số điểm muốn rút không được để trống")
    @DecimalMin(value = "1", message = "Số điểm muốn rút phải lớn hơn 0")
    private BigDecimal amount;

    private String note;
}
