package com.mmall.controller.common.interceptor;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by tttppp606 on 2019/3/6.
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //HandlerMethod：请求中Controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        //1、解析HandlerMethod，获取被拦截 方法的名字 和 被拦截的类的名字 ，用于日志和解决登陆拦截问题
        String methodName = handlerMethod.getMethod().getName();//如果请求manage/user/login.do，methodName就是 login，className就是UserManagerController
        String className = handlerMethod.getBean().getClass().getSimpleName();

        Map<String, String[]> parameterMap = request.getParameterMap();//自动默认是String和String[]
        //2、遍历请求报文的数据parameterMap获取key和value，用于日志
        Set<Map.Entry<String, String[]>> entrySet = parameterMap.entrySet();//
        Iterator<Map.Entry<String, String[]>> iterator = entrySet.iterator();
        StringBuffer stringBuffer = new StringBuffer();//线程安全
        while (iterator.hasNext()){
            Map.Entry<String, String[]> entry = iterator.next();
            String key = entry.getKey();
            String[] strings = entry.getValue();
            String value = Arrays.toString(strings);//代替String value = strings.toString();
            stringBuffer.append(key).append("=").append(value);
        }

        //3、代码层面解决登陆被拦截的情况，也可以在配置拦截器bean的xml中设置拦截路径
        if (StringUtils.equals(methodName,"login") && StringUtils.equals(className,"UserManageController")){
            log.info("拦截器放过登陆请求，methodName:{},className:{}",methodName,className);
            return true;//如果是登陆，就放过请求
        }

        log.info("拦截器拦截请求，methodName:{},className:{}，param:{}",methodName,className,stringBuffer);

        //权限判断
        User user = null;
        //5、loginToken == null
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isNotEmpty(loginToken)){
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.string2Obj(userJsonStr,User.class);
        }


        if(user == null || (user.getRole().intValue() != Const.Role.ROLE_ADMIN)) {
            //6、response响应报文的配置
            response.reset();//这里要添加reset，否则报异常 getWriter() has already been called for this response.
            response.setCharacterEncoding("UTF-8");//这里要设置编码，否则会乱码
            response.setContentType("application/json;charset=UTF-8");//这里要设置返回值的类型，因为全部是json接口。
            PrintWriter out = response.getWriter();

            //7、查询的用户信息为空user == null或者身份不是管理员
            if (user == null){
                //（1）富文本上传返回格式有要求，特殊处理
                if (StringUtils.equals(className,"ProductManageController") && StringUtils.equals(methodName,"richtextImgUpload")){
                    HashMap<Object, Object> map = Maps.newHashMap();
                    map.put("success",false);
                    map.put("msg","无此用户");
                    //这里不能直接out.print(map）;因为前端要求响应报文是Json格式的，
                    //此处用response响应，没有框架里的@ResponseBody，所以要先转为Json字符串再传输。
                    out.print(JsonUtil.obj2String(map));
                }else {
                    //（2）返回统一格式的响应报文
                    out.print(ServerResponse.createByErrorMessage("拦截器拦截，用户未登陆或用户不存在"));
                }
            }else {
                if (StringUtils.equals(className,"ProductManageController") && StringUtils.equals(methodName,"richtextImgUpload")) {
                    HashMap<Object, Object> map = Maps.newHashMap();
                    map.put("success", false);
                    map.put("msg", "用户无权限，请登陆管理员");
                    out.print(JsonUtil.obj2String(map));
                }else {
                    out.print(ServerResponse.createByErrorMessage("拦截器拦截，用户无权限"));
                }
            }
            out.flush();
            out.close();
            return false;//返回false.即不会调用controller里的方法
        }
        return true;//进入被拦截的控制器
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
