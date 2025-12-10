package com.ticket.interceptor;

import com.ticket.annotation.AdminRequired;
import com.ticket.common.RoleConstant;
import com.ticket.entity.User;
import com.ticket.mapper.UserMapper;
import com.ticket.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从Header获取Token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.sendError(401, "未登录");
            return false;
        }
        token = token.substring(7);

        // 2. 解析Token获取用户ID和角色
        Long userId = Long.valueOf(jwtUtil.getUserIdFromToken(token));
        String role = jwtUtil.getRoleFromToken(token); // 需扩展JWT工具类携带角色

        // 3. 验证管理端接口权限
        if (handler instanceof HandlerMethod handlerMethod) {
            AdminRequired adminRequired = handlerMethod.getMethodAnnotation(AdminRequired.class);
            if (adminRequired != null && !RoleConstant.ADMIN.equals(role)) {
                response.sendError(403, "无管理员权限");
                return false;
            }
        }

        // 4. 验证用户是否被禁用（status=0 表示禁用）
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            // TODO: 这里已经有非空判断了，controller中不需要重复判空
            response.sendError(403, "用户已被禁用");
            return false;
        }

        // 5. 存入请求属性供后续使用
        // TODO：可以存到上下文threadLocal里
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);
        return true;
    }
}