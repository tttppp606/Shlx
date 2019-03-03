package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by tttppp606 on 2019/2/15.
 */
@Controller
@RequestMapping("/order/")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private IOrderService iOrderService;

    /**
     * 商户系统生成订单后，将订单信息发送给支付宝，支付宝返回二维码地址，利用支付宝SDK将二维码
     * 地址转为二维码图片，存放在ftp服务器中，再返回二维码图片在ftp服务器的地址
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, long orderNo, HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createBySuccessMessage("用户未登陆");
        }
        String s = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(s, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //支付宝返回的二维码生成图片后保存在tomcat下的目录
        String path = session.getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId(),orderNo,path);
    }

    /**
     * 支付宝通知商户，客户是否付款成功，成功就返回success，失败就返回failed，支付宝会重复通知商户
     * @param request
     * @return
     */
    @RequestMapping(value = "alipay_callback.do",method = RequestMethod.POST)
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        //支付宝回调的数据都放在request，返回给商户
        //建立一个HashMap<String, String>，将支付宝返回的数据放入里面
        //request中的数据是键值对，并且值可能是字符串组
        //将request中的数据变为Map<String,String>
        HashMap<String, String> params = Maps.newHashMap();

        Map requestParams = request.getParameterMap();
        Set set = requestParams.keySet();//将map中的key提取出来，组合为一个set
        for (Iterator iter = set.iterator();iter.hasNext();){//利用迭代器遍历set
            String name = (String)iter.next();
            String[] values =(String[]) requestParams.get(name);//request的value可能是字符串组
            //将字符串数组values，变为以，分割的字符串valueStr
            String valueStr = "";
            for (int i = 0 ;i <values.length;i++){
                valueStr = (i == values.length - 1)?valueStr + values[i]:valueStr + values[i] + ",";
            }
            params.put(name,valueStr);
        }
        logger.info("支付宝回调，sign:{},trade_status:{},总参数:{}",params.get("sign"),params.get("trade_status"),params.toString());

        //利用支付宝的SDK验证数据是否是支付宝传递来的
        try {
            params.remove("sign_type");
            boolean rsaCheckV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if (!rsaCheckV2){
                return ServerResponse.createByErrorMessage("验证不通过，不是支付宝回调数据，非法请求，再请求就报警！！！");
            }
        } catch (AlipayApiException e) {
            logger.info("支付宝回调异常");
            e.printStackTrace();
        }
        //验证回调数据的正确性

        //验证成功后，更新订单表和生成支付信息表，在Service中实现
        ServerResponse serverResponse = iOrderService.aliCallback(params);
        if (serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    /**
     * 前台展示完二维码后，需要查看是否付款成功，然后进行下一步操作
     * @return
     */
    @RequestMapping(value = "query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpServletRequest request,Long orderNo){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createBySuccessMessage("用户未登陆");
        }
        String s = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(s, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse<Boolean> response = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if (response.isSuccess()){//前端要true和false，两种情况都是正常的，不是error
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }

    /**
     * 即将生成订单前的订单信息，还没有订单号，就是购物车中被勾选的商品信息。
     * @param request
     * @return
     */
    @RequestMapping(value = "get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createBySuccessMessage("用户未登陆");
        }
        String s = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(s, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }

    @RequestMapping(value = "create.do")
    @ResponseBody
    public ServerResponse create(HttpServletRequest request, Integer shippingId){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createBySuccessMessage("用户未登陆");
        }
        String s = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(s, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.create(user.getId(),shippingId);
    }

    @RequestMapping(value = "cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpServletRequest request,Long orderNo){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createBySuccessMessage("用户未登陆");
        }
        String s = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(s, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(),orderNo);
    }

    /**
     * 根据订单号，查询该订单的详情
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServerResponse detail(HttpServletRequest request,Long orderNo){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createBySuccessMessage("用户未登陆");
        }
        String s = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(s, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.detail(user.getId(),orderNo);
    }

    /**
     * 查看本用户的所有订单（已取消，未付款，已付款等所有状态）
     * @param request
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(HttpServletRequest request,
                                         @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createBySuccessMessage("用户未登陆");
        }
        String s = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(s, User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.list(user.getId(),pageNum,pageSize);
    }
}
