package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 因为session里currentUser（=data）这个key经常被引用，我们将这个字符串对象化，每次调用类的字段就可以
 * Created by tttppp606 on 2019/1/25.
 */
public class Const {
    //session中的key
    public static final String CURRENT_USER = "currentUser";
   //检查username和email是否重复时，checkVaild的两种类型
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    //密码提示问题通过，设定的token，防止验证之后一直可以修改密码的问题
    public static final String TOKEN_PREFIX = "token_";
   //检测是否是管理员
    public interface Role{
        int ROLE_CUSTOMER = 0;
        int ROLE_ADMIN = 1;
    }
    public interface RedisCacheExtime{
        int REDIS_SESSION_EXTIME = 60 * 30;//30分钟
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    public interface Cart{
        int CHECKED = 1;//即购物车选中状态
        int UN_CHECKED = 0;//购物车中未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public enum ProductStatusEnum {
        ON_SALE(1,"在线"),
        OFF_SALE(0,"下架");

        private int code;
        private String value;

        ProductStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }
        public void setCode(int code) {
            this.code = code;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }

        public static ProductStatusEnum codeof(int code){
            for (ProductStatusEnum productStatusEnum : values()){
                if (productStatusEnum.getCode() == code){
                    return productStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }



    }

    public interface AlipayCallback{
        //支付宝返回的交易状态status中的两种，其实有4中，只用了2种
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");


        private int code;
        private String value;

        OrderStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }
        public String getValue() {
            return value;
        }

        public static OrderStatusEnum codeof(int code){
            for (OrderStatusEnum orderStatusEnum : values()) {
                if (orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");

        private int code;
        private String value;

        PayPlatformEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }
        public String getValue() {
            return value;
        }
    }

    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"在线支付");

        private int code;
        private String value;
        PaymentTypeEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }
        public int getCode() {
            return code;
        }
        public String getValue() {
            return value;
        }

        public static PaymentTypeEnum codeof(int code){
            for (PaymentTypeEnum paymentTypeEnum : values()) {
                if (paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

}
