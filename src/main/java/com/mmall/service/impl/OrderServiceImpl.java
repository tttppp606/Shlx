package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by tttppp606 on 2019/2/15.
 */
@Service
public class OrderServiceImpl implements IOrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse pay(Integer userId, long orderNo, String path) {

        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("没有该订单");
        }

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("happymall扫码支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        // 对应实际付款金额，就是买家实际花了多少钱
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();//传入支付宝的商品详细信息list集合

        List<OrderItem> orderItemsList = orderItemMapper.selectByUserIdOrderNo(userId, orderNo);
        for (OrderItem orderItem : orderItemsList) {
            //支付宝要求传入的单价的单位为分，而数据库中的单价orderItem.getCurrentUnitPrice()为元，所以要用乘法运算，并且保证不丢失精度
            //支付宝要求传入的单价为long
            GoodsDetail goodsDetail = GoodsDetail.newInstance(// GoodsDetail.newInstance()参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
                    orderItem.getProductId().toString(),
                    orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100)).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goodsDetail);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知(回调）商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);


        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

//      执行请求builder返回了结果result
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");
                //
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);//打印到控制台

                //将返回的二维码生成图片上传到ftp服务器，并返回ftp二维码图片的url给前端
                String filePath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                File file = new File(filePath);//起好文件的路径和名字，不要忘记创建文件
                if (!file.exists()) {
                    file.setWritable(true);//linux上的权限
                    file.mkdirs();
                }
                logger.info("filePath:" + filePath);
                //支付宝返回的二维码持久化到filePath中
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                //上传ftp服务器
                ArrayList<File> files = new ArrayList<>();
                files.add(file);
                try {
                    FTPUtil.upLoad(files);
                } catch (IOException e) {
                    logger.error("二维码上传异常");
                    e.printStackTrace();
                }
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")  + file.getName();

                //新建map，返回给前端的数据
                HashMap<String, String> map = new HashMap<>();
                map.put("orderNo", order.getOrderNo().toString());
                map.put("qrUrl", qrUrl);
                return ServerResponse.createBySuccess(map);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    @Override
    public ServerResponse aliCallback(HashMap<String, String> params) {
        Long orderNo = Long.parseLong(params.get("out_trade_no"));//支付宝传入的是String，数据库里设计的是long
        String trade_status = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("无此订单");//告诉支付宝，传过来的数据里的订单号，在商户这里没有
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccessMessage("支付宝回调重复");//告诉支付宝，订单已经付款成功了，或者发货，或者交易关闭了
        }
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(trade_status)) {
            //更新order表中信息
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            //在controller中将data类型的转为String类型了，而数据库是date类型，需要把String类型转换为数据库的date类型
            order.setUpdateTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            orderMapper.updateByPrimaryKeySelective(order);
        }
        //生成支付信息表
        PayInfo payInfo = new PayInfo();
        payInfo.setOrderNo(orderNo);
        payInfo.setUserId(order.getUserId());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());//1为支付宝，2为微信
        payInfo.setPlatformNumber(params.get("trade_no"));
        payInfo.setPlatformStatus(params.get("trade_status"));
        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse<Boolean> queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("用户无该订单");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    //打印到控制台
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }










    /**
     * 即将生成订单前的订单信息，还没有订单号，就是购物车中被勾选的商品信息。
     * @param userId
     * @return
     */
    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        //1、设置ImageHost
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        //2、设置ProductTotalPrice
        ServerResponse<BigDecimal> paymentResult = this.getTotalProductPrice(userId);
        if (!paymentResult.isSuccess()){
            return paymentResult;
        }
        BigDecimal productTotalPrice = paymentResult.getData();
        orderProductVo.setProductTotalPrice(productTotalPrice);
        //3、设置OrderItemVoList
        ServerResponse<List<OrderItem>> orderItemListResult = getOrderItemList(userId);
        if (!orderItemListResult.isSuccess()){
            return orderItemListResult;
        }
        List<OrderItem> orderItemList = orderItemListResult.getData();
        List<OrderItemVo> orderItemVoList = this.assembleOrderItemVoList(orderItemList);
        orderProductVo.setOrderItemVoList(orderItemVoList);

        return ServerResponse.createBySuccess(orderProductVo);
    }

    /**
     * 创建订单
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse create(Integer userId, Integer shippingId) {
        //1、生成order表数据，有订单号，并且插入了数据库
        ServerResponse orderResult = this.getOrder(userId, shippingId);
        if (!orderResult.isSuccess()) {
            return orderResult;
        }
        Order order = (Order) orderResult.getData();

        // 2、生成orderItem表的数据orderItemList，没有订单号，也没有插入数据库
        ServerResponse<List<OrderItem>> orderItemListResult = getOrderItemList(userId);
        if (!orderItemListResult.isSuccess()){
            return orderItemListResult;
        }
        List<OrderItem> orderItemList = orderItemListResult.getData();
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        //Mybatis中集合批量插入数据库中!!
        orderItemMapper.batchInsert(orderItemList);
        // TODO: 2019/2/20 orderItemList插入数据库后，会创建时间，本方法中再调用orderItemList，应该从数据库中查询带有创建时间的orderItemList

        //3、减少库存
        for (OrderItem orderItem : orderItemList) {
            Integer productId = orderItem.getProductId();
            Product product = productMapper.selectByPrimaryKey(productId);
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
        //4、清空购物车
        for (OrderItem orderItem : orderItemList) {
            Integer productId = orderItem.getProductId();
            cartMapper.deleteByUserIdProductId(userId,productId);
        }
        //5、返回前端,组装orderVo
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {
        Order order  = orderMapper.selectByUserIdOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("该用户此订单不存在");
        }
        if(order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()){
            return ServerResponse.createByErrorMessage("已付款,无法取消订单");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());

        int row = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if(row > 0){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    /**
     * 根据订单号，查询该订单的详情
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse detail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("无此订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(userId,orderList);
        //pageResult中关于分页的数据（总页数，每页的第一个id。。。）都在查询orderList中得出
        PageInfo pageInfo = new PageInfo(orderList);
        //展示的是orderVoLis分页，将分页的内容由orderList换为orderVoLis
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }













//    后台

    @Override
    public ServerResponse<PageInfo> manageList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);

        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = this.assembleOrderVoList(null,orderList);

        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse manageSearch(Long orderNo, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("无此订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        pageInfo.setList(Lists.newArrayList(orderVo));
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse manageDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("无此订单号");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse<String> orderSendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (Const.OrderStatusEnum.SHIPPED.getCode() == order.getStatus()){
            return ServerResponse.createByErrorMessage("订单已发货");
        }
        order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
        order.setUpdateTime(new Date());
        orderMapper.updateByPrimaryKeySelective(order);
        return ServerResponse.createBySuccessMessage("订单状态修改为已发货");
    }


    /**
     * 生成订单表，并插入数据库
     * @return
     */
    private ServerResponse getOrder(Integer userId, Integer shippingId) {
        Order order = new Order();
        //a.生成订单号
        Long orderNo = this.getOrderNo();
        //b.计算实际应该付款金额
        ServerResponse<BigDecimal> result = this.getTotalProductPrice(userId);//getPayment里面做了判断，需要直接返回前端
        if (!result.isSuccess()) {
            return result;
        }
        BigDecimal payment = result.getData();
        //c.组装order
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        //判断订单里是否包含收货地址
        Shipping shipping = shippingMapper.selectByPrimaryKey(shippingId);
        if (shipping == null){
            return ServerResponse.createByErrorMessage("无收货地址");
        }
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPostage(0);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        //支付时间、交易完成时间、交易关闭时间，通过支付宝的通知再设置
        int insert = orderMapper.insert(order);
        if (insert == 0) {
            return ServerResponse.createByErrorMessage("生成订单失败");
        }
        //返回带有插入和更新日期的order
        Order order1 = orderMapper.selectByOrderNo(order.getOrderNo());
        return ServerResponse.createBySuccess(order1);
    }

    /**
     * 生成订单明细表,没有传入订单号，没有插入数据库
     * @return
     */
    private ServerResponse<List<OrderItem>> getOrderItemList(Integer userId) {
        List<OrderItem> orderItemList = new ArrayList<>();
        List<Cart> cartList = cartMapper.selectCartByUserIdCheck(userId);
        if (CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for (Cart cart : cartList) {
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            OrderItem orderItem = new OrderItem();
            if (cart.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMessage("商品"+product.getName()+"库存不足");
            }
            if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
                return ServerResponse.createByErrorMessage("商品"+product.getName()+"已下架");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(cart.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cart.getQuantity().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    /**
     * 生成订单号
     * @return
     */
    private Long getOrderNo() {
        long currentTimeMillis = System.currentTimeMillis();
        int random = new Random().nextInt(100);
        return currentTimeMillis + random;
    }

    /**
     * 计算购物车中被勾选的商品总价格
     * 即马上要生成的订单的商品总价格，并且对库存和是否上架做出了判断
     * @param userId
     * @return 因为要做一些判断，返回给前端，所以返回值累心是ServerResponse<BigDecimal>,
     * 如果返回的是ServerResponse，拿出的payment是Object类型，还需要转化为BigDecimal
     */
    private ServerResponse<BigDecimal> getTotalProductPrice(Integer userId) {
        List<Cart> cartList = cartMapper.selectCartByUserIdCheck(userId);
        if (cartList == null) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        BigDecimal totalProductPrice = new BigDecimal("0");//订单表数据库是BIgDecimal，返回前端是String
        for (Cart cart : cartList) {
            Integer productId = cart.getProductId();
            Product product = productMapper.selectByPrimaryKey(productId);
            BigDecimal price = product.getPrice();//单价
            Integer quantity = cart.getQuantity();//数量，cart是已经被勾选的购物车
            //商品库存和是否上架判定
            if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
                return ServerResponse.createByErrorMessage("商品" + product.getName() + "已下架");
            }
            if (quantity > product.getStock()) {
                return ServerResponse.createByErrorMessage("商品" + product.getName() + "库存不足");
            }

            BigDecimal mul = BigDecimalUtil.mul(price.doubleValue(), quantity.doubleValue());//单类商品价格
            totalProductPrice = BigDecimalUtil.add(mul.doubleValue(), totalProductPrice.doubleValue());
        }
        return ServerResponse.createBySuccess(totalProductPrice);
    }

    private List<OrderItemVo> assembleOrderItemVoList(List<OrderItem> orderItemList){
        List<OrderItemVo> orderItemVoList = new ArrayList<>();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = new OrderItemVo();

            orderItemVo.setOrderNo(orderItem.getOrderNo());
            orderItemVo.setProductId(orderItem.getProductId());
            orderItemVo.setProductName(orderItem.getProductName());
            orderItemVo.setProductImage(orderItem.getProductImage());
            orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
            orderItemVo.setQuantity(orderItem.getQuantity());
            orderItemVo.setTotalPrice(orderItem.getTotalPrice());
            orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));

            orderItemVoList.add(orderItemVo);
        }
        return orderItemVoList;
    }

    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();

        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }

    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeof(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeof(order.getStatus()).getValue());
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));

        orderVo.setOrderItemVoList(this.assembleOrderItemVoList(orderItemList));
        orderVo.setShippingId(order.getShippingId());

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        //assemble只管组装，订单里没有收货地址的信息，要生成订单，插入数据库前判断，并抛给前端
        if (shipping != null) {
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
            orderVo.setReceiverName(this.assembleShippingVo(shipping).getReceiverName());
        }

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix") + PropertiesUtil.getProperty("ftp.dir"));

        return orderVo;
    }

    private List<OrderVo> assembleOrderVoList(Integer userId,List<Order> orderList){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null){
                // 管理员查询的时候 不需要传userId
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            }else {
                orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, order.getOrderNo());
            }
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }


}
