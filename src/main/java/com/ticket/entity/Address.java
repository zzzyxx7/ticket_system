package com.ticket.entity;

import lombok.Data;

import java.util.Date;
@Data
public class Address {
    private Long id;
    private Long userId;
    private String recipientName;
    private String phone;
    private String addressDetail;
    private Boolean isDefault;
    private Date createdTime;

    
}