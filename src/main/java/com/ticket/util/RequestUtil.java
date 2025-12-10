package com.ticket.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 请求工具类：统一处理从 HTTP 请求中提取用户信息等操作
 */
public class RequestUtil {

    /**
     * 从请求中获取当前登录用户ID（从拦截器设置的request属性中获取）
     *
     * @param request HTTP请求对象
     * @return 当前用户ID，未登录则返回null
     */
    public static Long getUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            return null;
        }

        // 兼容 String 和 Long 两种类型
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof String) {
            try {
                return Long.valueOf((String) userIdObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从请求中获取当前用户角色
     *
     * @param request HTTP请求对象
     * @return 用户角色（USER/ADMIN），未登录则返回null
     */
    public static String getRole(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object roleObj = request.getAttribute("role");
        if (roleObj instanceof String) {
            return (String) roleObj;
        }
        return null;
    }
}

