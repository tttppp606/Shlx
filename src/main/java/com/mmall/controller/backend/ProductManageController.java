package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tttppp606 on 2019/1/30.
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IProductService iProductService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IFileService iFileService;

    @RequestMapping(value = "save.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productSave(HttpServletRequest request, Product product){
        return iProductService.addOrUpdateProduct(product);
    }

    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getDetail(HttpServletRequest request, Integer productId){
        return iProductService.manageProductDetail(productId);
    }

    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> getList(HttpServletRequest request, @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum, @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize ){
        return iProductService.getProductList(pageNum,pageSize);
    }

    @RequestMapping(value = "search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> productSearch(HttpServletRequest request,
                                                  Integer productId,
                                                  String productName,
                                                  @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                                  @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize ){
        return iProductService.searchProduct(productName,productId,pageNum,pageSize);
    }

    @RequestMapping(value = "upload.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(HttpServletRequest request, MultipartFile file,HttpSession session){//MultipartFile的file，必须与前端提交的file一致
//        path是F:/Program Files/apache-tomcat-7.0.75/webapps/ROOT/upload，只是指定了地址，没有实际创建这个文件
//        就是webapp的路径，也是项目的根路径
        String path = session.getServletContext().getRealPath("upload");
        return iFileService.upload(file,path) ;
    }

    @RequestMapping(value = "richtext_img_upload.do",method = RequestMethod.POST)
    @ResponseBody
    public Map richtextImgUpload(HttpServletRequest request,HttpSession session, MultipartFile file, HttpServletResponse response){//MultipartFile的file，必须与前端提交的file一致
        HashMap map = new HashMap<>();

//        String loginToken = CookieUtil.readLoginToken(request);
//        if (StringUtils.isEmpty(loginToken)){
//            map.put("success",false);
//            map.put("msg","用户未登陆");
//            return map;//前端要求是Json，这里返回Map是因为有@ResponseBody，他会把map转化为Json。
//        }
//        String s = RedisShardedPoolUtil.get(loginToken);
//        User user = JsonUtil.string2Obj(s, User.class);
//        if(user == null){
//            map.put("success",false);
//            map.put("msg","无此用户");
//            return map;
//        }
//        if (!iUserService.checkAdminRole(user).isSuccess()) {
//            map.put("success",false);
//            map.put("msg","普通用户，无权限操作");
//            return map;
//        }


//        返回的是F:\Program Files\apache-tomcat-7.0.75\webapps\ROOT\
//        就是webapp的路径，也是项目的根路径
        String path = session.getServletContext().getRealPath("upload");
        Map data = (Map) iFileService.upload(file, path).getData();
        if (data == null || data.isEmpty()){
            map.put("success",false);
            map.put("msg","上传失败");
            return map;
        }
        String url = (String)data.get("url");
        map.put("success",true);
        map.put("msg","上传成功");
        map.put("file_path",url);
        //响应头中加入的键值对，是与前端的要求
        response.addHeader("Access-Control-Allow-Headers","X-File-Name");
        return map;
    }

    @RequestMapping(value = "set_sale_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setSaleStatus(HttpServletRequest request, Integer productId, Integer status){//MultipartFile的file，必须与前端提交的file一致
        return iProductService.setSaleStatus(productId,status);
    }




}
