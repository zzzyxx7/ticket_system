package com.ticket.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.common.Result;
import com.ticket.entity.User;
import com.ticket.mapper.UserMapper;
import com.ticket.util.JwtUtil;
import com.ticket.util.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 
 * 作用：替代原来的 JwtInterceptor 和 AuthInterceptor
 * 
 * 工作流程：
 * 1. 从 Header 中提取 token
 * 2. 验证 token 是否有效
 * 3. 解析 token 获取 userId 和 role
 * 4. 检查用户是否存在、是否被禁用
 * 5. 将用户信息存入 SecurityContext（Spring Security 的上下文）
 * 6. 继续执行后续的过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();

        // 排除不需要认证的路径（这些路径直接放行，不走 JWT 验证）
        if (shouldSkipAuthentication(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 从 Header 中获取 token
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // 没有 token，返回 401
                sendUnauthorizedResponse(response, "未提供token");
                return;
            }

            // 2. 提取 token（去掉 "Bearer " 前缀）
            String token = authHeader.substring(7);

            // 3. 验证 token 是否有效
            if (!jwtUtil.validateToken(token)) {
                sendUnauthorizedResponse(response, "token无效或已过期");
                return;
            }

            // 4. 从 token 中解析用户ID和角色
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            Long userId = Long.valueOf(userIdStr);

            // 5. 检查用户是否存在、是否被禁用
            User user = userMapper.selectById(userId);
            if (user == null) {
                sendForbiddenResponse(response, "用户不存在");
                return;
            }
            
            if (user.getStatus() == 0) {
                sendForbiddenResponse(response, "用户已被禁用");
                return;
            }

            // 6. 将用户信息存入 UserContext（你原来的 ThreadLocal）
            UserContext.setUserId(userId);
            UserContext.setRole(role);

            // 7. 将用户信息存入 request attribute（兼容你现有的 RequestUtil）
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);

            // 8. 创建 Spring Security 的认证对象，存入 SecurityContext
            // 这样 Spring Security 就知道当前用户是谁、有什么权限了
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,  // 主体（principal）：用户ID
                    null,    // 凭证（credentials）：密码，JWT 场景下不需要
                    Collections.singletonList(authority)  // 权限列表：角色
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // 存入 SecurityContext，这样后续的 Controller 和 Service 都能通过 SecurityContext 获取当前用户
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 9. 继续执行后续的过滤器
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 如果处理过程中出错，返回 401
            sendUnauthorizedResponse(response, "认证失败: " + e.getMessage());
        }
    }

    /**
     * 判断这个路径是否需要跳过认证
     */
    private boolean shouldSkipAuthentication(String requestURI) {
        // 登录接口
        if (requestURI.equals("/api/user/auth")) {
            return true;
        }
        // 首页接口（允许未登录访问）
        if (requestURI.equals("/api/event/home")) {
            return true;
        }
        // Swagger 相关路径
        if (requestURI.startsWith("/swagger") ||
            requestURI.startsWith("/v2/api-docs") ||
            requestURI.startsWith("/webjars/")) {
            return true;
        }
        return false;
    }

    /**
     * 返回 401 未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<String> result = Result.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * 返回 403 禁止访问响应
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        Result<String> result = Result.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

