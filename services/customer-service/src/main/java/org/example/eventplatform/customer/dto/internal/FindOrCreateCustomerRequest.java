package org.example.eventplatform.customer.dto.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindOrCreateCustomerRequest {

    @NotNull
    private Long tenantId;

    @NotBlank
    private String phone;

    private Long userId;

    private String fullName;

    private String email;
}
