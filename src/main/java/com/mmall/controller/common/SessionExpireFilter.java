package com.mmall.controller.common;

import com.mmall.common.Const;
import com.mmall.util.CookieUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by tttppp606 on 2019/3/3.
 */
public class SessionExpireFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isNotEmpty(loginToken)){
            //当过了30min的有效期，缓存中没有loginToken = User，执行下面的命令只会返回0，不会报错
            RedisPoolUtil.expire(loginToken, Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {

    }
}
