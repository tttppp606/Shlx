package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tttppp606 on 2019/2/2.
 */
@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVoLimit = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVoLimit);
    }

    @Override
    public ServerResponse<CartVo> add(Integer productId, Integer count,Integer userId) {
        if (productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByProductIdUserId(userId,productId);
        if (cart == null){
            //购物车数据库中还没该商品，需执行insert
            //todo再判断该商品是否存在或者是否下线
            Cart cartItem = new Cart();
            cartItem.setChecked(Const.Cart.CHECKED);//默认加入数据库是被选中状态
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        }else {
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKey(cart);
        }

        //判断选购量与库存的关系，并更新购物车数据库
        //建立vo
        //return ServerResponse.createBySuccess(this.getCartVoLimit(userId));
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> update(Integer productId, Integer count, Integer userId) {
        if (productId == null ||count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByProductIdUserId(productId, userId);
        if (cart != null){
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> delete(String productIds, Integer userId) {
        if (StringUtils.isBlank(productIds)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //将字符串用，分割，并放入集合中
        List<String> productList = Splitter.on(",").splitToList(productIds);
//        String[] productS = productIds.split(",");
//        List<String> productList = new ArrayList<>();
//        for (int i = 0;i < productS.length;i++){
//            productList.add(productS[i]);
//        }
        cartMapper.deleteByUserIdProductIds(userId,productList);
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, int checked) {
        cartMapper.updateCheckOrUncheck(userId,productId,checked);
        return this.list(userId);
    }

    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
       if (userId == null){
           return ServerResponse.createBySuccess(0);
       }
        int count = cartMapper.selectCartProductCount(userId);
        return ServerResponse.createBySuccess(count);
    }


    /**
     * 通过user_Id获取购物车列表，
     * 会做购物数量和库存的判断，
     * 会计算购物车里商品的总价格（被选上的商品check =1 才会计算价格）
     * 返回购物车详细信息CartVo
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId){
        if(userId == null){
            return null;
        }
        //1、查询购物车数据库，获得cartList
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        if (CollectionUtils.isEmpty(cartList)){
            return null;
        }
        //2、获取cartProductVoList
        ArrayList<CartProductVo> cartProductVoList = new ArrayList<>();
        BigDecimal totalPrice = new BigDecimal("0");//String参数，以后计算不会丢失精度
        // 遍历cartList
        for (Cart cartItem : cartList) {
                //组装cartProductVo
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null){
                    CartProductVo cartProductVo = new CartProductVo();
                    cartProductVo.setId(cartItem.getId());
                    cartProductVo.setUserId(userId);
                    cartProductVo.setProductId(cartItem.getProductId());
                    //判断购物车数量和库存数量关系
                    Integer stock = product.getStock();
                    if (cartItem.getQuantity() > stock){
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //更新购物车数据库，将数量限定为最大库存数
                        Cart cart = new Cart();
                        cart.setId(cartItem.getId());
                        cart.setQuantity(stock);
                        cartMapper.updateByPrimaryKeySelective(cart);
                    }
                    cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    cartProductVo.setQuantity(cartItem.getQuantity());

                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductStock(product.getStock());
                    cartProductVo.setProductChecked(cartItem.getChecked());
                    //计算单个商品金额
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    //迭代组装cartProductVoList
                    cartProductVoList.add(cartProductVo);
                    //迭代计算购物车中被勾选商品的总价格
                    if (cartProductVo.getProductChecked() == Const.Cart.CHECKED){
                        totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                    }
                }else {//如果购物车里的商品，在product表不存在，就删除购物车表的数据，保证购物车表的数据正确。
                    cartMapper.deleteByUserIdProductId(userId,cartItem.getProductId());
                }
        }
        //3、组装cartVo
        CartVo cartVo = new CartVo();
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setCartTotalPrice(totalPrice);
        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId){
        if (userId == null){
            return false;
        }
        int check_offRow = cartMapper.selectCartByUserIdAndCheck_Off(userId);
        return check_offRow == 0;
    }
}
