package com.ticket.controller;

// src/main/java/com/ticket/controller/AdminController.java
import com.ticket.annotation.AdminRequired;  // 暂时保留，后续可以删除
import com.ticket.common.Result;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
import com.ticket.entity.User;
import com.ticket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin") // 管理端接口统一前缀
public class AdminController{
    @Autowired
    private UserService userService;

    // 1. 管理端获取所有用户列表（不分页）
    @GetMapping("/users")
    @AdminRequired  // 暂时保留，后续可以删除（权限由 SecurityConfig 统一配置）
    public Result<List<User>> getAllUsers() {
        return userService.getAllUsers();
    }

    // 2. 分页查询用户信息（可按用户名、状态筛选）
    @GetMapping("/user/page")
    @AdminRequired
    public Result<PageResult<User>> getUsersByPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            PageRequest pageRequest) {
        PageResult<User> page = userService.getUsersByPage(username, status, pageRequest);
        return Result.success(page);
    }

    // 3. 查询用户详情（按ID）
    @GetMapping("/user/{id}")
    @AdminRequired
    public Result<User> getUserDetail(@PathVariable Long id) {
        return userService.getUserDetailForAdmin(id);
    }

    // 4. 按用户名查询用户详情
    @GetMapping("/user/username/{username}")
    @AdminRequired
    public Result<User> getUserByUsernameForAdmin(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    // 5. 修改用户信息（管理端可修改所有字段，包括 role、status）
    // 使用示例：
    // - 修改角色：PUT /admin/user/{id}，Body: {"role": "ADMIN"} 或 {"role": "USER"}
    // - 修改其他信息：PUT /admin/user/{id}，Body: {"username": "xxx", "email": "xxx", ...}
    @PutMapping("/user/{id}")
    @AdminRequired
    public Result<String> updateUserInfo(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.updateUserForAdmin(user);
    }

    // 6. 启用 / 禁用用户（单独接口，更常用且语义清晰）
    @PutMapping("/user/{id}/status")
    @AdminRequired
    public Result<String> updateUserStatus(@PathVariable Long id,
                                           @RequestParam Integer status) {
        return userService.updateUserStatus(id, status);
    }

    // 7. 管理员删除用户
    @DeleteMapping("/user/{id}")
    @AdminRequired
    public Result<String> deleteUserByAdmin(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}