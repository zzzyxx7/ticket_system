package com.ticket.interceptor;

import com.ticket.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 放行登录接口
        String requestURI = request.getRequestURI();

        if (requestURI.equals("/user/auth")) {
            return true;
        }

        // 放行Swagger文档相关路径（如果集成了Swagger）
        if (requestURI.startsWith("/swagger") ||
                requestURI.startsWith("/v2/api-docs") ||
                requestURI.startsWith("/webjars/")) {
            return true;
        }

        // 获取token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"未提供token\"}");
            return false;
        }

        // 去除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\"}");
            return false;
        }
// 3. 解析用户ID和角色
        String userIdStr = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        // 统一存为 Long 类型，与其他拦截器保持一致
        Long userId = Long.valueOf(userIdStr);
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        // 4. 权限控制：管理端接口必须是ADMIN角色
        if (requestURI.startsWith("/admin/")) { // 管理端接口统一前缀/admin
            if (!"ADMIN".equals(role)) {
                response.setStatus(403);
                response.getWriter().write("{\"code\":403,\"message\":\"无管理员权限\"}");
                return false;
            }
        }

        return true;
    }
}