package org.example.eventplatform.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.example.eventplatform.customer.entity.CustomerType;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String email;
    private String address;
    private CustomerType type;
    private String note;

    // Admin chọn thành viên nào phụ trách khách này
    private Long assignedToUserId;
}
