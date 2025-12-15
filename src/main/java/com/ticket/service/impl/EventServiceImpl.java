package com.ticket.service.impl;

import com.ticket.common.EventCategoryConstant;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private EventConvertor eventConvertor;

    @Override
    public Result<EventDTO> getEventById(Long id) {
        Event event = eventMapper.selectById(id);
        if (event == null) {
            return Result.error("演出不存在");
        }
        EventDTO dto = eventConvertor.toDTO(event);
        // 用户端：只返回是否有库存（布尔值），隐藏具体库存数字
        setUserSideStockInfo(dto, event);

        // 是否开票：根据 status 判断，例如 PUBLISHED=已开票
        dto.setIssued("PUBLISHED".equals(event.getStatus()));

        return Result.success(dto);
    }

    @Override
    @Transactional
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
    @Transactional
    public Result<String> updateEvent(Long id, Event event, Long userId) {
        try {
            Event existingEvent = eventMapper.selectById(id);
            if (existingEvent == null) {
                return Result.error("演出不存在");
            }
            // TODO：还是去学一些关于权限管理的框架或者概念RBAC，可以自己用AOP实现一个小的权限管理框架
            // TODO: Satoken(比SpringSecurity简单配置一些)
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
    @Transactional
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

    /**
     * 设置用户端库存信息：只返回是否有库存（布尔值），隐藏具体库存数字
     * @param dto EventDTO对象
     * @param event Event实体对象（包含原始库存数据）
     */
    private void setUserSideStockInfo(EventDTO dto, Event event) {
        // 设置是否有库存（布尔值）
        dto.setHasStock(event.getStock() != null && event.getStock() > 0);
        // 隐藏具体库存数字（用户端不需要知道具体数量）
        dto.setStock(null);
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

        // 转换为DTO并设置用户端库存信息（隐藏具体库存数字）
        List<EventDTO> dtoList = eventConvertor.toDTOList(events);
        for (int i = 0; i < dtoList.size(); i++) {
            setUserSideStockInfo(dtoList.get(i), events.get(i));
        }
        
        return new PageResult<>(dtoList, total, pageRequest);
    }

    @Override
    public Result<List<EventDTO>> getHomeEvents(String city) {
        // 获取四大类分类
        List<String> categories = Arrays.asList(EventCategoryConstant.getHomeCategories());
        
        // 查询该城市下四大类的演出
        List<Event> events = eventMapper.selectByCityAndCategories(city, categories);
        
        // 转换为DTO并设置用户端库存信息（隐藏具体库存数字）
        List<EventDTO> dtoList = eventConvertor.toDTOList(events);
        for (int i = 0; i < dtoList.size(); i++) {
            setUserSideStockInfo(dtoList.get(i), events.get(i));
        }
        
        return Result.success(dtoList);
    }

    @Override
    public PageResult<EventDTO> searchEventsByNameAndCondition(String keyword, String city, String category, PageRequest pageRequest) {
        validatePageParams(pageRequest);

        // 1. 查询符合条件的总条数
        // TODO：这个地方可能sql会比较慢，在数据量比较大的情况下，前后都有通配符会导致全表扫描查询，后面有时间可以看看怎么优化
        // TODO：而且total其实直接从下面的数据获取list的大小就好了，这样会导致重复查询
        Long total = eventMapper.countByNameAndCondition(keyword, city, category);
        
        // 2. 查询当前页的条件数据
        List<Event> events = eventMapper.selectByNameAndCondition(
                keyword,
                city,
                category,
                pageRequest.getOffset(),
                pageRequest.getSize()
        );

        // 转换为DTO并设置用户端库存信息（隐藏具体库存数字）
        List<EventDTO> dtoList = eventConvertor.toDTOList(events);
        for (int i = 0; i < dtoList.size(); i++) {
            setUserSideStockInfo(dtoList.get(i), events.get(i));
        }
        
        return new PageResult<>(dtoList, total, pageRequest);
    }

    @Override
    public Result<EventDTO> getEventByIdForAdmin(Long id) {
        Event event = eventMapper.selectById(id);
        if (event == null) {
            return Result.error("演出不存在");
        }
        EventDTO dto = eventConvertor.toDTO(event);
        // 管理端：保留完整库存信息，不隐藏
        dto.setHasStock(event.getStock() != null && event.getStock() > 0);
        dto.setIssued("PUBLISHED".equals(event.getStatus()));
        // stock 字段保留原始值，不设置为 null
        return Result.success(dto);
    }

    @Override
    public PageResult<EventDTO> getEventsByConditionAndPageForAdmin(String city, String category, PageRequest pageRequest) {
        validatePageParams(pageRequest);

        // 1. 查询符合条件的总条数
        Long total = eventMapper.countByCondition(city, category);
        // 2. 查询当前页的条件数据
        List<Event> events = eventMapper.selectByCondition(
                city,
                category,
                pageRequest.getOffset(),
                pageRequest.getSize()
        );

        // 转换为DTO（管理端：保留完整库存信息，不隐藏）
        List<EventDTO> dtoList = eventConvertor.toDTOList(events);
        for (int i = 0; i < dtoList.size(); i++) {
            EventDTO dto = dtoList.get(i);
            Event event = events.get(i);
            // 设置 hasStock 和 issued，但保留 stock 原始值
            dto.setHasStock(event.getStock() != null && event.getStock() > 0);
            dto.setIssued("PUBLISHED".equals(event.getStatus()));
            // stock 字段保留，不设置为 null
        }
        
        return new PageResult<>(dtoList, total, pageRequest);
    }
}

