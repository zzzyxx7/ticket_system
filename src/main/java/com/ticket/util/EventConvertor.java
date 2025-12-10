package com.ticket.util;

import com.ticket.dto.EventDTO;
import com.ticket.entity.Event;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventConvertor {

    // 单个实体转DTO
    public EventDTO toDTO(Event event) {
        if (event == null) {
            return null;
        }
        EventDTO dto = new EventDTO();
        // 复制核心业务字段（不包含审计字段）
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
        dto.setTicketGrade(event.getTicketGrade());

        return dto;
    }

    // 批量实体转DTO列表
    public List<EventDTO> toDTOList(List<Event> events) {
        return events.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}