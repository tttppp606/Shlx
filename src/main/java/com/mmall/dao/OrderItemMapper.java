package com.mmall.dao;

import com.mmall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> selectByUserIdOrderNo(@Param("userId") Integer userId, @Param("orderNo") long orderNo);

    OrderItem selectByOrderNoProductId(@Param("orderNo") Long orderNo, @Param("productId") Integer productId);

    void batchInsert(@Param("orderItemList")List<OrderItem> orderItemList);

    List<OrderItem> selectByOrderNo(Long orderNo);
}