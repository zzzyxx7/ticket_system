package com.ticket.service;

import com.ticket.common.Result;
import com.ticket.dto.EventDTO;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
import com.ticket.entity.Event;

import java.util.List;

public interface EventService {

    Result<EventDTO> getEventById(Long id);

    // 首页推荐：按城市 + 四大类查询
    Result<List<EventDTO>> getHomeEvents(String city);

    Result<String> createEvent(Event event, Long userId);

    Result<String> updateEvent(Long id, Event event, Long userId);

    Result<String> deleteEvent(Long id);

    PageResult<EventDTO> getEventsByConditionAndPage(String city, String category, PageRequest pageRequest);
    
    // 带关键词的条件分页查询（支持按名称/明星名搜索 + 城市/分类筛选）
    PageResult<EventDTO> searchEventsByNameAndCondition(String keyword, String city, String category, PageRequest pageRequest);
    
    // ===== 管理端专用方法：返回完整库存信息 =====
    /**
     * 管理端查询演出详情（返回完整库存信息，不隐藏）
     */
    Result<EventDTO> getEventByIdForAdmin(Long id);
    
    /**
     * 管理端条件分页查询演出列表（返回完整库存信息，不隐藏）
     */
    PageResult<EventDTO> getEventsByConditionAndPageForAdmin(String city, String category, PageRequest pageRequest);
}


