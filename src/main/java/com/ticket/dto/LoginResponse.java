package com.ticket.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserInfo userInfo;


@Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;


    }
}