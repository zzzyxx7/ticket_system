package com.ticket.controller;
import com.ticket.common.Result;
import com.ticket.dto.LoginRequest;
import com.ticket.dto.LoginResponse;
import com.ticket.entity.User;
import com.ticket.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 在UserController中修改获取用户信息接口
    @GetMapping("/info")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null) {
            return Result.error("用户未登录");
        }

        Long userId = Long.valueOf(userIdStr);
        return userService.getCurrentUser(userId);
    }
    @PostMapping("/add")
    public Result<String> addUser(@RequestBody User user) {
        return userService.addUser(user);
    }
    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}