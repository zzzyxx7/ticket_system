package com.ticket.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 请求工具类：统一处理从 HTTP 请求中提取用户信息等操作
 * 
 * 获取策略（优先级从高到低）：
 * 1. 优先从 UserContext（ThreadLocal）获取（性能更好，无需传 request 参数）
 * 2. 如果没有，再从 request attribute 获取（向后兼容）
 */
public class RequestUtil {

    /**
     * 从请求中获取当前登录用户ID
     * 
     * 获取顺序：
     * 1. 优先从 UserContext（ThreadLocal）获取
     * 2. 如果没有，再从 request attribute 获取（向后兼容）
     *
     * @param request HTTP请求对象（可以为 null，如果 UserContext 中有值）
     * @return 当前用户ID，未登录则返回null
     */
    public static Long getUserId(HttpServletRequest request) {
        // 1. 优先从 UserContext 获取（无需传 request 参数，性能更好）
        Long userIdFromContext = UserContext.getUserId();
        if (userIdFromContext != null) {
            return userIdFromContext;
        }

        // 2. 如果没有，再从 request attribute 获取（向后兼容）
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
     * 获取顺序：
     * 1. 优先从 UserContext（ThreadLocal）获取
     * 2. 如果没有，再从 request attribute 获取（向后兼容）
     *
     * @param request HTTP请求对象（可以为 null，如果 UserContext 中有值）
     * @return 用户角色（USER/ADMIN），未登录则返回null
     */
    public static String getRole(HttpServletRequest request) {
        // 1. 优先从 UserContext 获取
        String roleFromContext = UserContext.getRole();
        if (roleFromContext != null) {
            return roleFromContext;
        }

        // 2. 如果没有，再从 request attribute 获取（向后兼容）
        if (request == null) {
            return null;
        }
        Object roleObj = request.getAttribute("role");
        if (roleObj instanceof String) {
            return (String) roleObj;
        }
        return null;
    }

    /**
     * 直接从 UserContext 获取当前用户ID（无需传 request 参数）
     * 推荐在 Service 层使用此方法，避免传递 HttpServletRequest 参数
     * 
     * @return 当前用户ID，未登录则返回null
     */
    public static Long getCurrentUserId() {
        return UserContext.getUserId();
    }

    /**
     * 直接从 UserContext 获取当前用户角色（无需传 request 参数）
     * 推荐在 Service 层使用此方法，避免传递 HttpServletRequest 参数
     * 
     * @return 用户角色（USER/ADMIN），未登录则返回null
     */
    public static String getCurrentRole() {
        return UserContext.getRole();
    }
}

