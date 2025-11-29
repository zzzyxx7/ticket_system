package com.ticket.dto;

import lombok.Data;

@Data
public class UserAuthResponse {
    private String token; // JWT令牌
    private Long userId; // 用户ID
}