package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;

import java.util.HashMap;

/**
 * Created by tttppp606 on 2019/2/15.
 */
public interface IOrderService {
    //支付宝
    ServerResponse pay(Integer userId, long orderNo, String path);
    ServerResponse aliCallback(HashMap<String, String> params);
    ServerResponse<Boolean> queryOrderPayStatus(Integer userId,Long orderNo);
    //普通用户订单
    ServerResponse create(Integer userId, Integer shippingId);
    ServerResponse cancel(Integer userId, Long orderNo);
    ServerResponse getOrderCartProduct(Integer userId);
    ServerResponse detail(Integer userId, Long orderNo);
    ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize);
    //后台订单
    ServerResponse<PageInfo> manageList(Integer pageNum, Integer pageSize);
    ServerResponse<PageInfo> manageSearch(Long orderNo, Integer pageNum, Integer pageSize);

    ServerResponse manageDetail(Long orderNo);

    ServerResponse<String> orderSendGoods(Long orderNo);
}
