// src/main/java/com/ticket/mapper/EventMapper.java
package com.ticket.mapper;

import com.ticket.entity.Event;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EventMapper {
    // 基础CRUD
    Event selectById(Long id);
    List<Event> selectAll();
    int insert(Event event);
    int update(Event event);
    int deleteById(Long id);

    // 条件查询
    List<Event> selectByCity(String city);
    List<Event> selectByCategory(String category);
    List<Event> selectByCityAndCategory(@Param("city") String city,
                                        @Param("category") String category);

    // 搜索功能
    List<Event> searchByName(String keyword);


    Long countEvents();
    List<Event> selectByPage(@Param("offset") int offset, @Param("size") int size);
    List<Event> selectByCondition(@Param("city") String city,
                                  @Param("category") String category,
                                  @Param("offset") int offset,
                                  @Param("size") int size);
    Long countByCondition(@Param("city") String city, @Param("category") String category);


    int decreaseStock(@Param("eventId") Long eventId,
                      @Param("quantity") Integer quantity);

}