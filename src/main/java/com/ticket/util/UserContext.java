package com.ticket.util;

/**
 * 请求线程上下文，存储当前登录用户的基础信息。
 * 使用 ThreadLocal 确保同一请求线程内可随取随用，其他线程不可见。
 */
public final class UserContext {
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE_HOLDER = new ThreadLocal<>();

    private UserContext() {
        // 工具类不允许实例化
    }

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void setRole(String role) {
        ROLE_HOLDER.set(role);
    }

    public static String getRole() {
        return ROLE_HOLDER.get();
    }

    /**
     * 请求结束后务必清理，避免线程复用导致数据泄漏。
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        ROLE_HOLDER.remove();
    }
}
