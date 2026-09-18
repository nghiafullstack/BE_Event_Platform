package org.example.eventplatform.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventplatform.customer.entity.CustomerType;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private CustomerType type;
    private String note;
    private boolean active;
    private Long assignedToUserId;
    private Long tenantId;
    private LocalDateTime createdAt;
}
