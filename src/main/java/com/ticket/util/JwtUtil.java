package com.ticket.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "ticket-system-secret-key-2023-web-group-assessment-256"; // 保持不变（≥256位）
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 7天有效期

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // 新增：生成包含用户ID和角色的token（用于权限控制）
    public String generateToken(String userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId); // 自定义载荷存储用户ID
        claims.put("role", role); // 自定义载荷存储角色（如"USER"/"ADMIN"）
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    // 保留原有方法（仅用户ID），兼容旧逻辑
    public String generateToken(String userId) {
        return Jwts.builder()
                .subject(userId) // 原有逻辑：用subject存储用户ID
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    // 新增：从token中解析角色
    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class); // 从自定义载荷获取角色
    }

    // 优化：统一解析claims，兼容新旧token格式（旧：subject存userId；新：claims存userId）
    public String getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        // 优先从自定义载荷获取，兼容旧版从subject获取
        String userId = claims.get("userId", String.class);
        return userId != null ? userId : claims.getSubject();
    }

    // 优化：统一解析token的通用方法（减少重复代码）
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 以下方法保持不变
    public boolean validateToken(String token) {
        try {
            parseClaims(token); // 复用parseClaims
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}