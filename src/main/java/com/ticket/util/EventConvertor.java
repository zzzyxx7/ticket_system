package com.ticket.util;

import com.ticket.dto.EventDTO;
import com.ticket.entity.Event;
import com.ticket.entity.User;
import com.ticket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventConvertor {

    @Autowired
    private UserMapper userMapper;  // 用于查询创建人用户名

    // 单个实体转DTO
    public EventDTO toDTO(Event event) {
        if (event == null) {
            return null;
        }
        EventDTO dto = new EventDTO();
        // 复制核心业务字段
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());
        dto.setCity(event.getCity());
        dto.setCategory(event.getCategory());
        dto.setVenue(event.getVenue());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setPrice(event.getPrice());
        dto.setStock(event.getStock());
        dto.setStatus(event.getStatus());

        // 复制审计字段
        dto.setCreatedTime(event.getCreatedTime());
        dto.setCreatedBy(event.getCreatedBy());
        dto.setUpdatedTime(event.getUpdatedTime());


        return dto;
    }

    // 批量实体转DTO列表
    public List<EventDTO> toDTOList(List<Event> events) {
        return events.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}