package com.ticket.dto;

import lombok.Data;

@Data
public class PageRequest {
    private Integer page = 1;      // 当前页码，默认第1页
    private Integer size = 10;     // 每页大小，默认10条
    private String sort;           // 排序字段
    private String order;          // 排序方向：asc/desc

    public PageRequest(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }
    public Integer getOffset() {
        return (page - 1) * size;
    }

}