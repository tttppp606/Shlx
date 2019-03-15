package com.mmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by tttppp606 on 2019/3/5.
 */
@Slf4j
@Component
public class ExceptionResolver implements HandlerExceptionResolver {
    //重写的方法返回类型是ModelAndView，可以选择Json格式输出前端，也可以选择跳转到直接错误界面
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());
        log.error("{}出现异常",request.getRequestURI(),ex);//必须有，否则后台也看不到真实错误原因
        modelAndView.addObject("status","1");
        modelAndView.addObject("msg","出现异常，请查看服务器日志");
        modelAndView.addObject("data", ex.getMessage());//ex.getMessage()只会打印一行日志
        return modelAndView;
    }



}
