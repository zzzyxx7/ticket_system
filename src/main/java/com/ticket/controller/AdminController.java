package com.ticket.controller;

// src/main/java/com/ticket/controller/AdminController.java
import com.ticket.annotation.AdminRequired;
import com.ticket.common.Result;
import com.ticket.entity.User;
import com.ticket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin") // 管理端接口统一前缀
public class AdminController {

    @Autowired
    private UserService userService;

    // 管理员查询所有用户（需要管理员权限）
    @GetMapping("/users")
    @AdminRequired // 标记需要管理员权限
    public Result<List<User>> getAllUsers() {
        return userService.getAllUsers(); // 需要在UserService中实现
    }

    // 管理员修改用户角色（例如将普通用户升级为管理员）
    @PutMapping("/user/{id}/role")
    @AdminRequired
    public Result<String> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        return userService.updateUserRole(id, role); // 需要在UserService中实现
    }

    // 其他管理端接口：例如演出审核、订单管理等
}