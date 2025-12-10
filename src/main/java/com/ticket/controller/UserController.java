package com.ticket.controller;
import com.ticket.common.Result;
import com.ticket.dto.UserAuthRequest;
import com.ticket.dto.UserAuthResponse;
import com.ticket.entity.User;
import com.ticket.service.UserService;
import com.ticket.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return userService.getCurrentUser(userId);
    }
    
    @PostMapping("/auth")
    public Result<UserAuthResponse> auth(@RequestBody @Valid UserAuthRequest request) {
        UserAuthResponse response = userService.auth(request);
        return Result.success(response);
    }
    
    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 用户端只能修改自己的信息
        Long currentUserId = RequestUtil.getUserId(request);
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        
        // 强制设置为当前用户ID，防止修改他人信息
        user.setId(currentUserId);
        // 用户端不允许修改 role 和 status，清空这些字段
        user.setRole(null);
        user.setStatus(null);
        
        return userService.updateUser(user);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        // 暂时不写后端逻辑，前端直接清除LocalStorage中的Token

        return Result.success("退出登录成功");
    }


}