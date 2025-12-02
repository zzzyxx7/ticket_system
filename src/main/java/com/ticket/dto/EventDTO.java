package com.ticket.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class EventDTO {
    private Long id;
    private String name;
    private String description;
    private String city;
    private String category;
    private String venue;
    private Date startTime;
    private Date endTime;
    private BigDecimal price;
    private Integer stock;
    private String status;
    private String ticketGrade;
    private Date createdTime;
    private Long createdBy;
    private Long updatedBy;
    private Date updatedTime;

    // 用户端汇总字段：
    private Boolean hasStock; // stock > 0
    private Boolean issued;   // status 是否为已开票状态
}
