
package com.ticket.dto;

import java.math.BigDecimal;

public class CreateOrderRequest {
    private Long eventId;
    private Integer quantity;
    private BigDecimal totalPrice;

    // getter和setter
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}