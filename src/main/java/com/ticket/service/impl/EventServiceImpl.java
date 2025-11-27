package com.ticket.service.impl;

import com.ticket.common.Result;
import com.ticket.dto.EventDTO;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
import com.ticket.entity.Event;
import com.ticket.mapper.EventMapper;
import com.ticket.service.EventService;
import com.ticket.util.AuditUtil;
import com.ticket.util.EventConvertor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private AuditUtil auditUtil;
    @Autowired
    private EventConvertor eventConvertor;

    @Override
    public Result<List<EventDTO>> getAllEvents() {
        List<Event> events = eventMapper.selectAll();
        // 转换为DTO列表
        return Result.success(eventConvertor.toDTOList(events));
    }

    @Override
    public Result<EventDTO> getEventById(Long id) {
        Event event = eventMapper.selectById(id);
        if (event == null) {
            return Result.error("演出不存在");
        }
        // 转换为DTO
        return Result.success(eventConvertor.toDTO(event));
    }

    @Override
    public Result<List<EventDTO>> getEventsByCity(String city) {
        List<Event> events = eventMapper.selectByCity(city);
        return Result.success(eventConvertor.toDTOList(events));
    }

    @Override
    public Result<List<EventDTO>> getEventsByCategory(String category) {
        List<Event> events = eventMapper.selectByCategory(category);
        return Result.success(eventConvertor.toDTOList(events));
    }

    @Override
    public Result<List<EventDTO>> searchEvents(String city, String category) {
        List<Event> events;
        if (city != null && category != null) {
            events = eventMapper.selectByCityAndCategory(city, category);
        } else if (city != null) {
            events = eventMapper.selectByCity(city);
        } else if (category != null) {
            events = eventMapper.selectByCategory(category);
        } else {
            events = eventMapper.selectAll();
        }
        return Result.success(eventConvertor.toDTOList(events));
    }

    @Override
    public Result<List<EventDTO>> searchEventsByName(String keyword) {
        List<Event> events = eventMapper.searchByName(keyword);
        return Result.success(eventConvertor.toDTOList(events));
    }

    @Override
    public Result<String> createEvent(Event event, Long userId) {
        try {
            // 替换直接设置createdBy的方式，使用工具类统一处理
            AuditUtil.setCreateAuditFields(event, userId);  // 需要改造AuditUtil支持传入userId
            eventMapper.insert(event);
            return Result.success("演出创建成功，演出ID: " + event.getId());
        } catch (Exception e) {
            return Result.error("演出创建失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> updateEvent(Long id, Event event, Long userId) {
        try {
            Event existingEvent = eventMapper.selectById(id);
            if (existingEvent == null) {
                return Result.error("演出不存在");
            }
            event.setId(id);
            // 替换直接设置updatedBy的方式，使用工具类统一处理
            AuditUtil.setUpdateAuditFields(event, userId);  // 改造AuditUtil支持传入userId
            eventMapper.update(event);
            return Result.success("演出更新成功");
        } catch (Exception e) {
            return Result.error("演出更新失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> deleteEvent(Long id) {
        try {
            Event event = eventMapper.selectById(id);
            if (event == null) {
                return Result.error("演出不存在");
            }
            eventMapper.deleteById(id);
            return Result.success("演出删除成功");
        } catch (Exception e) {
            return Result.error("演出删除失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<EventDTO> getEventsByPage(PageRequest pageRequest) {
        validatePageParams(pageRequest);

        Long total = eventMapper.countEvents();
        List<Event> events = eventMapper.selectByPage(
                pageRequest.getOffset(), pageRequest.getSize());

        List<EventDTO> dtoList = eventConvertor.toDTOList(events);
        return new PageResult<>(dtoList, total, pageRequest);
    }

    private void validatePageParams(PageRequest pageRequest) {
        if (pageRequest.getPage() == null || pageRequest.getPage() < 1) {
            pageRequest.setPage(1);
        }
        if (pageRequest.getSize() == null || pageRequest.getSize() < 1) {
            pageRequest.setSize(10);
        }
        if (pageRequest.getSize() > 100) {
            pageRequest.setSize(100);
        }
    }

    @Override
    public PageResult<EventDTO> getEventsByConditionAndPage(String city, String category, PageRequest pageRequest) {
        validatePageParams(pageRequest); // 复用分页参数验证

        // 1. 查询符合条件的总条数
        Long total = eventMapper.countByCondition(city, category);
        // 2. 查询当前页的条件数据
        List<Event> events = eventMapper.selectByCondition(
                city,
                category,
                pageRequest.getOffset(),
                pageRequest.getSize()
        );

        List<EventDTO> dtoList = eventConvertor.toDTOList(events);
        return new PageResult<>(dtoList, total, pageRequest);
    }
}

