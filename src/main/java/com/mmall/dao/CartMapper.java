package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    List<Cart> selectByUserId(Integer userId);

    int selectCartByUserIdAndCheck_Off(Integer userId);

    Cart selectCartByProductIdUserId(@Param("userId") Integer userId,@Param("productId") Integer productId);

    int deleteByUserIdProductIds(@Param("userId")Integer userId, @Param("productList")List<String> productList);

    int deleteByUserIdProductId(@Param("userId")Integer userId, @Param("productId") Integer productId);

    int updateCheckOrUncheck(@Param("userId")Integer userId, @Param("productId") Integer productId,@Param("checked")int checked);

    int selectCartProductCount(Integer userId);

    List<Cart> selectCartByUserIdCheck(Integer userId);
}