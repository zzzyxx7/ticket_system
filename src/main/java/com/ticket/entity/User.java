package com.ticket.entity;
import lombok.Data;

import java.util.Date;
@Data
public class User {
    private Long id;
    private String username; // 昵称
    private String password;
    private String email;
    private String phone;
    private Date createdTime;
    private Date updatedTime;
    private String role;
    private Integer status;     // 新增：用户状态（1-启用，0-禁用，管理端用）


}