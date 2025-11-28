
package com.ticket.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;




@Data
public class CreateOrderRequest {
    @NotNull(message = "演出ID不能为空")
    private Long eventId;

    @NotNull(message = "购买数量不能为空")
    @Positive(message = "购买数量必须大于0")
    private Integer quantity;

    //@NotNull(message = "总金额不能为空")
    //@Positive(message = "总金额必须大于0")
    //private BigDecimal totalPrice; // 新增校验：非空且为正数
}