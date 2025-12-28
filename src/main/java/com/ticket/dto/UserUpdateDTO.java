package com.ticket.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    // 不包含 id（从 token 中获取，不信任前端传来的 id）
    // 不包含 role、status（用户端不允许修改）
    private String username;
    private String email;
    private String phone;
}