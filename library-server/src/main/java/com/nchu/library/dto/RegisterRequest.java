package com.nchu.library.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegisterRequest {
    @NotBlank(message = "学号不能为空")
    private String studentId;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少需要6位")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String phone;
}