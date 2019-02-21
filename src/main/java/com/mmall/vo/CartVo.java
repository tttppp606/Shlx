package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by tttppp606 on 2019/2/2.
 */
public class CartVo {

    private List<CartProductVo> cartProductVoList;
    private boolean allChecked;
    private BigDecimal cartTotalPrice;

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public boolean isAllChecked() {
        return allChecked;
    }

    public void setAllChecked(boolean allChecked) {
        this.allChecked = allChecked;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }
}
