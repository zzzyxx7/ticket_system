package com.ticket.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    // 不包含 role、status（用户端不允许修改）
    private Long id;
    private String username;
    private String email;
    private String phone;
}