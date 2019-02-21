package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

import java.util.List;

/**
 * Created by tttppp606 on 2019/2/2.
 */
public interface ICartService {
    ServerResponse<CartVo> list(Integer userId);

    ServerResponse<CartVo> add(Integer productId, Integer count,Integer userId);

    ServerResponse<CartVo> update(Integer productId, Integer count, Integer userId);

    ServerResponse<CartVo> delete(String productIds, Integer userId);

    ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, int checked);

    ServerResponse<Integer> getCartProductCount(Integer userId);
}
