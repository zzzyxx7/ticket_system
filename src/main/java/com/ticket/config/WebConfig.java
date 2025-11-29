package com.ticket.config;


import com.ticket.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns("/user/auth")  // 排除认证接口(登录/注册)
                .excludePathPatterns("/swagger**/**")  // 排除Swagger
                .excludePathPatterns("/webjars/**")
                .excludePathPatterns("/v2/api-docs");
    }
}