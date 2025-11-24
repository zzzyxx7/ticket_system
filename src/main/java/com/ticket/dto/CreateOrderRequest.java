
package com.ticket.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class CreateOrderRequest {
    private Long eventId;
    private Integer quantity;
    private BigDecimal totalPrice;


}