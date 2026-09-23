package org.example.eventplatform.identity.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Khách thuê sự kiện tự đăng ký. Khác tài khoản nội bộ ở chỗ không thuộc đoàn nào
 * (tenant = null) vì khách duyệt và đặt show của nhiều đoàn khác nhau.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRegisterRequest {

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0")
    private String phone;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    private String email;
}
