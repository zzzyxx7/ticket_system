package com.ticket.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UserAuthRequest {
    @NotBlank(message = "账号不能为空（用户名或邮箱）")
    private String account; // 支持用户名或邮箱

    @NotBlank(message = "密码不能为空")
    private String password;
}