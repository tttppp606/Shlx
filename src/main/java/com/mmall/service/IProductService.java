package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

/**
 * Created by tttppp606 on 2019/1/30.
 */
public interface IProductService {
    ServerResponse addOrUpdateProduct(Product product);

    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize);

    ServerResponse<PageInfo> searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize);

    ServerResponse getProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductByKeywordCategoryId(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy);

    ServerResponse setSaleStatus(Integer productId, Integer status);
}
