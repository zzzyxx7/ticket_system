package com.ticket.mapper;

import com.ticket.entity.TicketOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketOrderMapper {
    // 基础CRUD
    TicketOrder selectById(Long id);
    List<TicketOrder> selectAll();
    int insert(TicketOrder order);
    int update(TicketOrder order);
    int deleteById(Long id);

    // 业务查询
    List<TicketOrder> selectByUserId(@Param("userId") Long userId);
    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("updatedBy") Long updatedBy);

    // 分页查询
    List<TicketOrder> selectByPage(@Param("offset") int offset, @Param("size") int size);
    List<TicketOrder> selectByUserIdAndPage(@Param("userId") Long userId,
                                            @Param("offset") int offset,
                                            @Param("size") int size);
    
    // 用户端条件分页查询
    List<TicketOrder> selectByUserCondition(@Param("userId") Long userId,
                                            @Param("status") String status,
                                            @Param("eventId") Long eventId,
                                            @Param("offset") int offset,
                                            @Param("size") int size);
    Long countByUserCondition(@Param("userId") Long userId,
                              @Param("status") String status,
                              @Param("eventId") Long eventId);

    List<TicketOrder> selectByAdminCondition(@Param("userId") Long userId,
                                             @Param("status") String status,
                                             @Param("eventId") Long eventId,
                                             @Param("offset") int offset,
                                             @Param("size") int size);

    Long countByAdminCondition(@Param("userId") Long userId,
                               @Param("status") String status,
                               @Param("eventId") Long eventId);

    // 管理端更新订单（只更新允许字段）
    int updateByAdmin(TicketOrder order);
}