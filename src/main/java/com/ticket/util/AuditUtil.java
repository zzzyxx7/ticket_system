package com.ticket.util;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

@Component
public class AuditUtil {

    /**
     * 从请求中获取当前用户ID
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr != null) {
            return Long.valueOf(userIdStr);
        }
        return null;
    }

    /**
     * 设置创建审计字段
     */
    public static void setCreateAuditFields(Object entity, HttpServletRequest request) {
        if (entity instanceof com.ticket.entity.Event) {
            com.ticket.entity.Event event = (com.ticket.entity.Event) entity;
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId != null) {
                event.setCreatedBy(currentUserId);
            }
        }
    }

    /**
     * 设置更新审计字段
     */
    public static void setUpdateAuditFields(Object entity, HttpServletRequest request) {
        if (entity instanceof com.ticket.entity.Event) {
            com.ticket.entity.Event event = (com.ticket.entity.Event) entity;
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId != null) {
                event.setUpdatedBy(currentUserId);
            }
        }
    }
}