package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by tttppp606 on 2019/2/20.
 */
public class OrderProductVo {
    private BigDecimal productTotalPrice;
    private String imageHost;
    private List<OrderItemVo> orderItemVoList;

    public BigDecimal getProductTotalPrice() {
        return productTotalPrice;
    }

    public void setProductTotalPrice(BigDecimal productTotalPrice) {
        this.productTotalPrice = productTotalPrice;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public List<OrderItemVo> getOrderItemVoList() {
        return orderItemVoList;
    }

    public void setOrderItemVoList(List<OrderItemVo> orderItemVoList) {
        this.orderItemVoList = orderItemVoList;
    }
}
