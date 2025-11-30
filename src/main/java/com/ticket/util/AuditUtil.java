package com.ticket.util;

import com.ticket.entity.Address;
import com.ticket.entity.Event;
import com.ticket.entity.TicketOrder;
import com.ticket.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 审计工具类：统一处理实体类的创建人、创建时间、更新人、更新时间等审计字段
 */
@Component
public class AuditUtil {

    /**
     * 从请求中获取当前登录用户ID（从JwtInterceptor设置的request属性中获取）
     *
     * @param request HTTP请求对象
     * @return 当前用户ID，未登录则返回null
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return Long.valueOf(userIdStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 统一设置创建审计字段（createdBy、createdTime）
     * 支持的实体类型：Event、User、Address、TicketOrder
     *
     * @param entity  实体对象
     * @param request HTTP请求对象（用于获取当前用户ID）
     */
    public static void setCreateAuditFields(Object entity, HttpServletRequest request) {
        if (entity == null) {
            return;
        }
        Long currentUserId = getCurrentUserId(request);
        Date currentTime = new Date();

        // 处理Event实体
        if (entity instanceof Event) {
            Event event = (Event) entity;
            if (currentUserId != null) {
                event.setCreatedBy(currentUserId);
                event.setCreatedTime(currentTime);
            }
            event.setCreatedTime(currentTime);
        }
        // 处理User实体
        else if (entity instanceof User) {
            User user = (User) entity;
            user.setCreatedTime(currentTime);
            // 用户创建人可根据业务需求设置，此处暂不设置（如管理员创建用户）
        }
        // 处理Address实体
        else if (entity instanceof Address) {
            Address address = (Address) entity;
            address.setCreatedTime(currentTime);
            address.setUpdatedBy(currentUserId); // 创建时更新人=创建人
            address.setCreatedTime(currentTime);
            // 地址创建人通常为关联的userId，已在Service层设置
        }
        // 处理TicketOrder实体
        else if (entity instanceof TicketOrder) {
            TicketOrder order = (TicketOrder) entity;
            order.setCreatedTime(currentTime);
            order.setUpdatedBy(currentUserId); // 创建时更新人=创建人
            order.setUpdatedTime(currentTime);
            // 订单创建人通常为关联的userId，已在Service层设置
        }
    }

    /**
     * 统一设置更新审计字段（updatedBy、updatedTime）
     * 支持的实体类型：Event、User、Address、TicketOrder
     *
     * @param entity  实体对象
     * @param request HTTP请求对象（用于获取当前用户ID）
     */
    public static void setUpdateAuditFields(Object entity, HttpServletRequest request) {
        if (entity == null) {
            return;
        }
        Long currentUserId = getCurrentUserId(request);
        Date currentTime = new Date();

        // 处理Event实体
        if (entity instanceof Event) {
            Event event = (Event) entity;
            if (currentUserId != null) {
                event.setUpdatedBy(currentUserId);
                event.setUpdatedTime(currentTime);
            }
            event.setUpdatedTime(currentTime);
        }
        // 处理User实体
        else if (entity instanceof User) {
            User user = (User) entity;
            user.setUpdatedTime(currentTime);
            // 用户更新人通常为自身ID，可根据业务需求扩展
        }
        // 处理Address实体
        else if (entity instanceof Address) {
            Address address = (Address) entity;
            address.setUpdatedBy(currentUserId);
            address.setUpdatedTime(currentTime);
            // 地址更新时间可在此设置，更新人通常为关联的userId
        }
        // 处理TicketOrder实体
        else if (entity instanceof TicketOrder) {
            TicketOrder order = (TicketOrder) entity;
            order.setCreatedBy(currentUserId);
            order.setUpdatedBy(currentUserId);
            order.setUpdatedTime(currentTime);
            // 订单更新时间可在此设置，更新人通常为关联的userId
        }
    }

    /**
     * 重载方法：直接通过用户ID设置创建审计字段（适用于无request对象的场景）
     *
     * @param entity  实体对象
     * @param userId  当前用户ID
     */
    public static void setCreateAuditFields(Object entity, Long userId) {
        if (entity == null) {
            return;
        }
        Date currentTime = new Date();

        if (entity instanceof Event) {
            Event event = (Event) entity;
            event.setCreatedBy(userId);
            event.setCreatedTime(currentTime);
        } else if (entity instanceof User) {
            User user = (User) entity;
            user.setCreatedTime(currentTime);
        } else if (entity instanceof Address) {
            Address address = (Address) entity;
            address.setCreatedTime(currentTime);
        } else if (entity instanceof TicketOrder) {
            TicketOrder order = (TicketOrder) entity;
            order.setCreatedTime(currentTime);
        }
    }

    /**
     * 重载方法：直接通过用户ID设置更新审计字段（适用于无request对象的场景）
     *
     * @param entity  实体对象
     * @param userId  当前用户ID
     */
    public static void setUpdateAuditFields(Object entity, Long userId) {
        if (entity == null) {
            return;
        }
        Date currentTime = new Date();

        if (entity instanceof Event) {
            Event event = (Event) entity;
            event.setUpdatedBy(userId);
            event.setUpdatedTime(currentTime);
        } else if (entity instanceof User) {
            User user = (User) entity;
            user.setUpdatedTime(currentTime);
        }
    }
}