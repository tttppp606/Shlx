package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tttppp606 on 2019/1/28.
 */
@Service
public class CategoryServiceImpl implements ICategoryService {
    Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//这个分类是可用的

        int insert = categoryMapper.insert(category);
        if(insert > 0){
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
//        updateByPrimaryKeySelective有选择的更新某些列
        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount > 0){
            return ServerResponse.createBySuccess("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer parentId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(parentId);
        if (CollectionUtils.isEmpty(categoryList)){
            //如果没找到categoryList，说明前端传过来的categoryId是最底层的，没有子类，这时不能报错，而是输出日志，返回null
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    @Override
    public ServerResponse<List<Integer>> getCurrentCategoryIdAndDeepChildrenCategoryId(Integer categoryId) {
        HashSet<Category> totalCategorySet = new HashSet<>();
        findChildrenCategoryList(totalCategorySet, categoryId);
//      totalCategorySet不会为null，如果categoryId存在，肯定有返回值，如果categoryId不存在，也会返回[]
        List<Integer> integers = new ArrayList<>();
        if(categoryId != null){
            for (Category category : totalCategorySet) {
                Integer id = category.getId();
                integers.add(id);
            }
        }
        return ServerResponse.createBySuccess(integers);
    }

    private Set<Category> findChildrenCategoryList(Set<Category> totalCategorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category == null){
            return totalCategorySet;
            }
        totalCategorySet.add(category);
        //查询下一级分类的数据，放入集合中
        List<Category> childrenList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        /**   MyBatis查询不到结果，返回值也不是null,而是一个没有元素的空集合，所以不需要对结果集合判断是否为null*/
        //遍历下一级分类，查询下下一级分类的数据，并递归查询
        for (Category category1 : childrenList) {
            findChildrenCategoryList(totalCategorySet,category1.getId());
        }
        return totalCategorySet;
    }

}
