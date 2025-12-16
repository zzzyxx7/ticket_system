package com.ticket.controller;
import com.ticket.common.Result;
import com.ticket.dto.UserAuthRequest;
import com.ticket.dto.UserAuthResponse;
import com.ticket.dto.UserDTO;
import com.ticket.dto.UserUpdateDTO;
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
    public Result<UserDTO> getCurrentUser(HttpServletRequest request) {
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
    public Result<String> updateUser(@RequestBody @Valid UserUpdateDTO dto, HttpServletRequest request) {
        // 用户端只能修改自己的信息
        Long currentUserId = RequestUtil.getUserId(request);
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        
        // 要求前端必须传 id
        if (dto.getId() == null) {
            return Result.error("用户ID不能为空");
        }

        // 判断传进来的 userId 和当前登录用户是否一致
        if (!dto.getId().equals(currentUserId)) {
            return Result.error("不允许修改其他用户的信息");
        }

        // DTO 里不包含 role、status 字段，前端无法传递，更安全
        return userService.updateUser(dto);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        // 暂时不写后端逻辑，前端直接清除LocalStorage中的Token

        return Result.success("退出登录成功");
    }


}