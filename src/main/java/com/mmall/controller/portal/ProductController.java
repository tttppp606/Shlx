package com.mmall.controller.portal;

import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * Created by tttppp606 on 2019/2/1.
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;


    //适合改为Restful
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId){
        return iProductService.getProductDetail(productId);
    }
    @RequestMapping(value = "/{productId}",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> detailRestful(@PathVariable Integer productId){
        return iProductService.getProductDetail(productId);
    }



    //不适合改Restful，因为参数不一定能全部传过来
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "categoryId",required = false)Integer categoryId,//对于不一定要传值的参数，最好规定required，有利于程序的阅读
                               @RequestParam(value = "keyword",required = false)String keyword,
                               @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                               @RequestParam(value = "orderBy",defaultValue = "")String orderBy){
        return iProductService.getProductByKeywordCategoryId(keyword,categoryId,pageNum,pageSize,orderBy);
    }

    //               /product/100002/Apple/1/10/price_asc，不能有空的，必须全有
    @RequestMapping(value = "/{categoryId}/{keyword}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse listRestful(@PathVariable(value = "categoryId")Integer categoryId,//对于不一定要传值的参数，最好规定required，有利于程序的阅读
                               @PathVariable(value = "keyword")String keyword,
                               @PathVariable(value = "pageNum") Integer pageNum,
                               @PathVariable(value = "pageSize")Integer pageSize,
                               @PathVariable(value = "orderBy")String orderBy){
        if (pageNum == null){
            pageNum = 1;
        }
        if (pageSize == null){
            pageSize = 10;
        }
        if (orderBy == null){
            orderBy = "price_asc";
        }
        return iProductService.getProductByKeywordCategoryId(keyword,categoryId,pageNum,pageSize,orderBy);
    }
    //               /product/categoryId/100002/1/10/price_asc
    @RequestMapping(value = "/categoryId/{categoryId}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse listRestfulNoKeyWord(@PathVariable(value = "categoryId")Integer categoryId,//对于不一定要传值的参数，最好规定required，有利于程序的阅读
                                      @PathVariable(value = "pageNum") Integer pageNum,
                                      @PathVariable(value = "pageSize")Integer pageSize,
                                      @PathVariable(value = "orderBy")String orderBy){
        if (pageNum == null){
            pageNum = 1;
        }
        if (pageSize == null){
            pageSize = 10;
        }
        if (orderBy == null){
            orderBy = "price_asc";
        }
        return iProductService.getProductByKeywordCategoryId(null,categoryId,pageNum,pageSize,orderBy);
    }
    //               /product/keyword/pple/1/10/price_asc
    @RequestMapping(value = "/keyword/{keyword}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse listRestfulNoCategoryId(@PathVariable(value = "keyword")String keyword,
                                                  @PathVariable(value = "pageNum") Integer pageNum,
                                                  @PathVariable(value = "pageSize")Integer pageSize,
                                                  @PathVariable(value = "orderBy")String orderBy){
        if (pageNum == null){
            pageNum = 1;
        }
        if (pageSize == null){
            pageSize = 10;
        }
        if (orderBy == null){
            orderBy = "price_asc";
        }
        return iProductService.getProductByKeywordCategoryId(keyword,null,pageNum,pageSize,orderBy);
    }
}
