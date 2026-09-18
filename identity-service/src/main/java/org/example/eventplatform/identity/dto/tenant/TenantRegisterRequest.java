package org.example.eventplatform.identity.dto.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TenantRegisterRequest {

    @NotBlank(message = "Tên đơn vị không được để trống")
    @Size(max = 255, message = "Tên quá dài")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String domain;

    @NotBlank(message = "Username cho admin không được để trống")
    private String adminUsername;

    @NotBlank(message = "Mật khẩu cho admin không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String adminPassword;
}
