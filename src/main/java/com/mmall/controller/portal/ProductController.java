package com.mmall.controller.portal;

import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;



/**
 * Created by tttppp606 on 2019/2/1.
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;

    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId){
        return iProductService.getProductDetail(productId);
    }

    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "categoryId",required = false)Integer categoryId,//对于不一定要传值的参数，最好规定required，有利于程序的阅读
                               @RequestParam(value = "keyword",required = false)String keyword,
                               @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                               @RequestParam(value = "orderBy",defaultValue = "")String orderBy){
        return iProductService.getProductByKeywordCategoryId(keyword,categoryId,pageNum,pageSize,orderBy);
    }
}
