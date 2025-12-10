package com.ticket.config;


import com.ticket.interceptor.AuthInterceptor;
import com.ticket.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    @NonNull
    private JwtInterceptor jwtInterceptor;

    @Autowired
    @NonNull
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns("/api/user/auth")  // 排除认证接口(登录/注册)
                .excludePathPatterns("/api/event/home") // 首页演出列表允许未登录访问
                .excludePathPatterns("/swagger**/**")  // 排除Swagger(先预留着，现在暂时使用Apifox)
                .excludePathPatterns("/webjars/**")
                .excludePathPatterns("/v2/api-docs");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/user/auth")
                .excludePathPatterns("/api/event/home")
                .excludePathPatterns("/swagger**/**")
                .excludePathPatterns("/webjars/**")
                .excludePathPatterns("/v2/api-docs");

    }
}