package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



/**
 * Created by tttppp606 on 2019/1/30.
 */
@Service
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;
//  service中用到另一个service方法，相当于要new一个service类，调用方法，所以要注入
    @Autowired
    private ICategoryService iCategoryService;

    @Override
    public ServerResponse addOrUpdateProduct(Product product) {
        if (product != null) {
            if (StringUtils.isNotBlank(product.getSubImages())) {
                //以，分割为数组,这里的逗号必须与请求中的逗号对应
                String[] subImageArray = product.getSubImages().split("，|,");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }

            if (product.getId() == null) {
                //id == null 新增数据
                int resultCount = productMapper.insertSelective(product);
                if (resultCount > 0) {
                    return ServerResponse.createBySuccessMessage("添加产品成功");
                }
            }
            //id != null 更新数据
            int resultCount = productMapper.updateByPrimaryKeySelective(product);
            if (resultCount > 0) {
                return ServerResponse.createBySuccessMessage("更新产品成功");
            }
        }
        return ServerResponse.createByErrorMessage("更新产品失败，参数错误");
    }

    @Override
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已删除或下架");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize) {
        //startPage--start
        //填充自己的sql查询逻辑
        //pageHelper-收尾
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectList();
        ArrayList<ProductListVo> productListVoList = new ArrayList<>();
        for (Product product : productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 没有符合条件的就传回空list和分页的信息
     * 没有传productName，只传了正确的productId，会返回productId的商品
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
//        if(StringUtils.isNotBlank(productName)){
//            productName = new StringBuilder().append("%").append(productName).append("%").toString();
//        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
        ArrayList<ProductListVo> productListVoList = new ArrayList<>();
        for (Product product : productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServerResponse.createByErrorMessage("商品已下架或删除");
        }
//        常量封装
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("商品已下架或删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponse<PageInfo> getProductByKeywordCategoryId(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy) {
//        if (StringUtils.isBlank(keyword) && categoryId == null){
//            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
//        }
/**
 *  1、先在category表查询符合categoryId的
 */
//      查询出当前categoryId和递归子节点的所有categoryId的集合
        ServerResponse<List<Integer>> currentCategoryIdAndDeepChildrenCategoryId = iCategoryService.getCurrentCategoryIdAndDeepChildrenCategoryId(categoryId);
        List<Integer> categoryList = currentCategoryIdAndDeepChildrenCategoryId.getData();
/**
 *  2、再在product表用结果集合查符合keyword的
 */
        List<Product> productList = productMapper.selectByKeywordAndCategoryList(keyword, categoryList);
/**
 *  3、查询的总结果分页
 */
        PageHelper.startPage(pageNum,pageSize);
//        告诉分页插件如何排序
        if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
            String[] split = orderBy.split("_");//将orderby里的内容以_分割成字符串数组
            PageHelper.orderBy(split[0]+""+split[1]);//PageHelper.orderBy()参数必须是列表名 desc/asc，例如price desc和price asc
        }
//        告诉分页插件分页内容
        ArrayList<ProductDetailVo> productDetailVoList = new ArrayList<>();
        for (Product product : productList) {
            ProductDetailVo productDetailVo = this.assembleProductDetailVo(product);
            productDetailVoList.add(productDetailVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productDetailVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse setSaleStatus(Integer productId, Integer status) {
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServerResponse.createByErrorMessage("无此商品");
        }
        product.setStatus(status);
        product.setUpdateTime(new Date());
        productMapper.updateByPrimaryKeySelective(product);
        return ServerResponse.createBySuccessMessage("商品状态修改为" + Const.ProductStatusEnum.codeof(status).getValue());
    }

    //pojp---->vo
    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();

        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        Product product1 = productMapper.selectByPrimaryKey(product.getCategoryId());
        if (product1 == null) {
            productDetailVo.setParentCategoryId(0);//默认根节点
        } else {
            productDetailVo.setParentCategoryId(product1.getCategoryId());
        }
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));

        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }

    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }
}
