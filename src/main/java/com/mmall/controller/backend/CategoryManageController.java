package com.mmall.controller.backend;

import com.mmall.common.ServerResponse;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by tttppp606 on 2019/1/28.
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping(value = "add_Category.do")
    @ResponseBody
    public ServerResponse addCategory(String categoryName, @RequestParam(value = "parentId",defaultValue = "0") Integer parentId, HttpServletRequest request){
        return iCategoryService.addCategory(categoryName, parentId);
    }

    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpServletRequest request,Integer categoryId,String categoryName){
        return iCategoryService.updateCategoryName(categoryId,categoryName);

    }

    /**
     * 获取下一级所有的元素的category库数据
     * @param request
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getCategory(HttpServletRequest request,@RequestParam(value = "categoryId" ,defaultValue = "0")Integer categoryId){
        //获取这一级下面子级的category库
        return iCategoryService.getChildrenParallelCategory(categoryId);

    }

    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse<List<Integer>> getDeepCategory(HttpServletRequest request, Integer categoryId){
        //获取当前分类id及递归子节点categoryId
        return iCategoryService.getCurrentCategoryIdAndDeepChildrenCategoryId(categoryId);
    }
}
