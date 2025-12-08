package com.ticket.common;

public final class RoleConstant {
    
    // 私有构造函数，防止实例化
    private RoleConstant() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    public static final String USER = "USER"; // 普通用户
    public static final String ADMIN = "ADMIN"; // 管理员
}