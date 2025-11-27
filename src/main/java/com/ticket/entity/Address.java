package com.ticket.entity;

import lombok.Data;

import java.util.Date;
@Data
public class Address {
    private Long id;
    private Long userId;
    private String recipientName;//收件人姓名
    private String phone;
    private String addressDetail;
    private Boolean isDefault;//是否默认地址
    private Long createdBy;
    private Date createdTime;
    private Long updatedBy;  // 最后修改人ID（关联user.id）
    private Date updatedTime; // 最后修改时间

    
}