package com.ticket.controller;
import com.ticket.common.Result;
import com.ticket.dto.UserAuthRequest;
import com.ticket.dto.UserAuthResponse;
import com.ticket.entity.User;
import com.ticket.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    @PostMapping("/auth")
    public Result<UserAuthResponse> auth(@RequestBody @Valid UserAuthRequest request) {
        UserAuthResponse response = userService.auth(request);
        return Result.success(response);
    }
    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        // 暂时不写后端逻辑，前端直接清除LocalStorage中的Token

        return Result.success("退出登录成功");
    }


}