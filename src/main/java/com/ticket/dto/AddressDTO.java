package com.ticket.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private Long userId;
    private String recipientName;//收件人姓名
    private String phone;
    private String addressDetail;
    private Boolean isDefault;//是否默认地址





}
