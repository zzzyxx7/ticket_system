package com.ticket.interceptor;

import com.ticket.annotation.AdminRequired;
import com.ticket.common.RoleConstant;
import com.ticket.entity.User;
import com.ticket.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取当前用户ID（从JWT解析后的request属性中）
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null) {
            response.sendError(401, "未登录");
            return false;
        }
        Long userId = Long.valueOf(userIdStr);

        // 2. 检查接口是否需要管理员权限
        if (handler instanceof HandlerMethod handlerMethod) {
            AdminRequired adminRequired = handlerMethod.getMethodAnnotation(AdminRequired.class);
            if (adminRequired != null) {
                // 3. 验证用户角色是否为管理员
                User user = userMapper.selectById(userId);
                if (user == null || !RoleConstant.ADMIN.equals(user.getRole())) {
                    response.sendError(403, "无管理员权限");
                    return false;
                }
            }
        }
        return true;
    }
}