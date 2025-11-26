package com.ticket.dto;

import lombok.Data;

import java.util.List;
@Data
public class PageResult<T> {
    private List<T> list;          // 当前页数据
    private Integer page;          // 当前页码
    private Integer size;          // 每页大小
    private Long total;            // 总记录数
    private Integer totalPages;    // 总页数

    public PageResult(List<T> list, Long total, PageRequest pageRequest) {
        this.list = list;
        this.page = pageRequest.getPage();
        this.size = pageRequest.getSize();
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / pageRequest.getSize());
    }

}