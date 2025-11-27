package com.ticket.service;

import com.ticket.common.Result;
import com.ticket.dto.EventDTO;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
import com.ticket.entity.Event;

import java.util.List;

public interface EventService {

    Result<List<EventDTO>> getAllEvents();

    Result<EventDTO> getEventById(Long id);

    Result<List<EventDTO>> getEventsByCity(String city);

    Result<List<EventDTO>> getEventsByCategory(String category);

    Result<List<EventDTO>>  searchEvents(String city, String category);

    Result<List<EventDTO>> searchEventsByName(String keyword);

    Result<String> createEvent(Event event, Long userId);

    Result<String> updateEvent(Long id, Event event, Long userId);

    Result<String> deleteEvent(Long id);

    PageResult<EventDTO> getEventsByPage(PageRequest pageRequest);

    PageResult<EventDTO> getEventsByConditionAndPage(String city, String category, PageRequest pageRequest);
}


