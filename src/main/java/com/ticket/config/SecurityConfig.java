package com.ticket.config;

import com.ticket.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 * 
 * 第二步：集成 JWT 认证过滤器
 * 
 * 说明：
 * 1. 将 JwtAuthenticationFilter 加入到 Spring Security 的过滤器链
 * 2. 配置哪些路径需要认证，哪些路径允许匿名访问
 * 3. 权限控制统一在配置类中管理（路径级别），不在 Controller 中使用 @PreAuthorize 注解
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置安全过滤器链
     * 
     * Spring Security 的过滤器链执行顺序：
     * 1. JwtAuthenticationFilter（我们自定义的，先执行）
     * 2. Spring Security 自己的过滤器（认证、授权等）
     * 3. Controller（最后执行）
     * 
     * 关于 Lambda 写法（必须用，因为 Spring Security 6.x 已经废弃了传统写法）：
     * - csrf -> csrf.disable() 意思是：给 csrf 参数，然后调用它的 disable() 方法
     * - 等价于传统写法：http.csrf().disable()
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（前后端分离项目通常不需要 CSRF 保护）
            // Lambda 写法：csrf -> csrf.disable()
            // 含义：接收一个 csrf 配置对象，调用它的 disable() 方法
            .csrf(csrf -> csrf.disable())
            
            // 设置会话策略为无状态（因为我们要用 JWT，不需要 Session）
            // STATELESS = 不创建 Session，每次请求都是独立的
            // Lambda 写法：session -> session.sessionCreationPolicy(...)
            // 含义：接收一个 session 配置对象，设置它的会话创建策略
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 将 JWT 认证过滤器添加到 Spring Security 过滤器链中
            // 放在 UsernamePasswordAuthenticationFilter 之前，这样会优先执行我们的 JWT 验证
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 配置请求授权规则
            // Lambda 写法：auth -> auth.requestMatchers(...).permitAll()
            // 含义：接收一个 auth 配置对象，配置哪些路径允许访问、哪些需要权限
            .authorizeHttpRequests(auth -> auth
                // 允许匿名访问的路径（这些路径不需要登录）
                .requestMatchers("/api/user/auth", "/api/event/home").permitAll()
                .requestMatchers("/swagger**/**", "/v2/api-docs", "/webjars/**").permitAll()
                
                // 管理端接口需要 ADMIN 角色（在配置类统一管理，Controller 中不需要加 @PreAuthorize 注解）
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 其他所有请求都需要认证（有有效的 token）
                .anyRequest().authenticated()
            );

        return http.build();
    }
}

