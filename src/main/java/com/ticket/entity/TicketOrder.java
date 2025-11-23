
package com.ticket.entity;

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
    private Date createdTime;


}