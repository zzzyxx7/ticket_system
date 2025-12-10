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
    int insert(Event event);
    int update(Event event);
    int deleteById(Long id);

    // 按城市 + 多个分类查询（用于首页推荐）
    List<Event> selectByCityAndCategories(@Param("city") String city,
                                          @Param("categories") List<String> categories);
    List<Event> selectByCondition(@Param("city") String city,
                                  @Param("category") String category,
                                  @Param("offset") int offset,
                                  @Param("size") int size);
    Long countByCondition(@Param("city") String city, @Param("category") String category);
    
    // 带关键词的条件分页查询
    List<Event> selectByNameAndCondition(@Param("keyword") String keyword,
                                         @Param("city") String city,
                                         @Param("category") String category,
                                         @Param("offset") int offset,
                                         @Param("size") int size);
    Long countByNameAndCondition(@Param("keyword") String keyword,
                                  @Param("city") String city,
                                  @Param("category") String category);


    int decreaseStock(@Param("eventId") Long eventId,
                      @Param("quantity") Integer quantity);

    /**
     * 回滚库存（订单取消时使用，使用乐观锁保证并发安全）
     * @param eventId 演出ID
     * @param quantity 回滚数量
     * @return 影响行数，0表示回滚失败（可能演出不存在）
     */
    int increaseStock(@Param("eventId") Long eventId,
                      @Param("quantity") Integer quantity);

}