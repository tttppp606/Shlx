package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by tttppp606 on 2019/1/25.
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired//根据类型找
    private IUserService iUserService;


    /**
     * 登陆，new Cookie，放缓存
     * @param username
     * @param password
     * @param session
     * @param httpServletResponse
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse){

        ServerResponse<User> response = iUserService.login(username, password);
        if(response.isSuccess()){
            //新建一个Cookie：COOKIE_NAME = session.getId()
            CookieUtil.writeLoginToken(httpServletResponse,session.getId());
            //将session.getId() = User对象 放入Redis缓存中
            RedisPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);

            // 返回的user放入session中，这样用户长时间登陆后，到时间会自动清除用户的sesion，防止长时间没在，还能修改密码的漏洞
            // session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }


    /**
     * 将Cookie里的login_token = xxxx删除，再次登陆时，获取不到login_token，就认为没有登陆
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest request,HttpServletResponse response){
        CookieUtil.delLoginToken(request,response);
        //session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccessMessage("登出成功");
    }

    /**
     * 注册
     * @param user
     * @return status，msg
     */
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    /**
     *检查用户名或者邮箱是否存在于数据库中：用于注册的时候验证用户名和邮箱重复
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type){
        return iUserService.checkValid(str,type);
    }

    /**
     * 获取登陆信息
     * @param request
     * @return
     */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest request){
        //从request的Cookie中获取login_token的值
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createBySuccessMessage("用户未登陆");
        }
        //用login_token的值获取Redis中的User对象
        String s = RedisPoolUtil.get(loginToken);
        //Json转User对象
        User user = JsonUtil.string2Obj(s, User.class);
        //防止往Cookie中手动添加loign_token,而被判断处于登录状态
        if (user == null){
            return ServerResponse.createByErrorMessage("无此用户");
        }
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 查找密码提示问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
//  public ServerResponse<String> forgetGetQuestion(String username){
    public ServerResponse forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    /**
     * 验证提示问题的答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse forgetCheckAnswer(String username, String question, String answer){
        return iUserService.checkAnswer(username,question,answer);
    }

    /**
     * 忘记密码时 重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
    }

    /**
     * 登陆后重置密码
     * @param request
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpServletRequest request,String passwordOld,String passwordNew){
//        判断是否登陆后时间过长（超过session的时间）或者未登陆
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createBySuccessMessage("用户未登陆");
        }
        String s = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(s, User.class);
        if (user == null){
            return ServerResponse.createByErrorMessage("无此用户");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    /**
     * 更新信息
     * @param request
     * @param user
     * @return
     */
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpServletRequest request,User user){
    //todo校验修改过的user中的名字和id与session中的一致
    //判断是否未登陆
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createBySuccessMessage("用户未登陆");
        }
        String s = RedisPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(s, User.class);
        if (currentUser == null){
            return ServerResponse.createByErrorMessage("无此用户");
        }
//        前端传过来的user中只包含邮箱，问题，答案，手机
        user.setId(currentUser.getId());
//        user.setUsername(currentUser.getUsername());
        ServerResponse<User> result = iUserService.updateInformation(user);
        if(result.isSuccess()){
//          补全session中的数据
            result.getData().setUsername(currentUser.getUsername());
            result.getData().setRole(currentUser.getRole());
            RedisPoolUtil.setEx(loginToken,JsonUtil.obj2String(result.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return result;
    }

    /**
     * 从数据库获取详细用户完整信息：便于以后扩展，不同于从session中简单的获取用户信息
     * 强制登陆：扩展使用！！！！！！！！！！！1
     * @param request
     * @return
     */
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getInformation(HttpServletRequest request){
        //判断是否登陆
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)){
            return ServerResponse.createBySuccessMessage("用户未登陆");
        }
        String s = RedisPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(s, User.class);
        if (currentUser == null){
            return ServerResponse.createByErrorMessage("无此用户");
        }
        if (currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录status=10");
        }
        //根据以后的扩展需求，前端调用这个方法的时候，默认已经在前端强制登陆了
        return iUserService.getInformation(currentUser.getId());
    }

}
