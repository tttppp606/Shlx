package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by tttppp606 on 2019/1/27.
 */
@Controller
@RequestMapping("/manage/user")
public class UserManageController {
    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
//        检查了名字和密码是否匹配，并且返回了user数据
        System.out.println(username+password+"");
        ServerResponse<User> response = iUserService.login(username, password);
        if(response.isSuccess()){
            if (response.getData().getRole() == Const.Role.ROLE_ADMIN){
                session.setAttribute(Const.CURRENT_USER,response.getData());
                return response;
            }else {
                return ServerResponse.createByErrorMessage("不是管理员,无法登录");
            }
        }
        //返回response！！！！！
        return response;
    }
}
