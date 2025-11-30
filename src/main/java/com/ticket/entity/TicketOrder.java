package com.ticket.entity;

import jakarta.websocket.Decoder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
public class TicketOrder {
    private Long id;
    private Long userId;
    private Long eventId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status; // PENDING, PAID, CANCELLED
    private Long createdBy;
    private Date createdTime;
    private Long updatedBy;  // 最后修改人ID（关联user.id）
    private Date updatedTime; // 最后修改时间

}