package com.ticket.entity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
public class Event {
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
    private Date createdTime;
    private Long createdBy;
    private Long updatedBy;
    private Date updatedTime;

}