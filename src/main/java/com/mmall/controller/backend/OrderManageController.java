package com.mmall.controller.backend;

import com.mmall.common.ServerResponse;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by tttppp606 on 2019/2/20.
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {
    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IUserService iUserService;

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpServletRequest request,
                               @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        return iOrderService.manageList(pageNum,pageSize);
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse search(HttpServletRequest request,Long orderNo,//增加分页是为了以后模糊查询扩展
                                 @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                 @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        return iOrderService.manageSearch(orderNo,pageNum,pageSize);
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(HttpServletRequest request,Long orderNo){
        return iOrderService.manageDetail(orderNo);
    }

    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse orderSendGoods(HttpServletRequest request,Long orderNo){
        return iOrderService.orderSendGoods(orderNo);
    }

}
