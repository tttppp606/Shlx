package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by tttppp606 on 2019/3/3.
 */
@Slf4j
public class CookieUtil {

    private static final String COOKIE_NAME = "login_token";
    private static final String COOKIE_DOMAIN = ".immoc.com";
    //private static final String DOMAIN = ".lichuang.xyz";



    //X:domain=".happymmall.com"
    //a:A.happymmall.com            cookie:domain=A.happymmall.com;path="/"
    //b:B.happymmall.com            cookie:domain=B.happymmall.com;path="/"
    //c:A.happymmall.com/test/cc    cookie:domain=A.happymmall.com;path="/test/cc"
    //d:A.happymmall.com/test/dd    cookie:domain=A.happymmall.com;path="/test/dd"
    //e:A.happymmall.com/test       cookie:domain=A.happymmall.com;path="/test"


    //"login_token" = session.getId()放入Cookie中，将Cookie放入response中
    public static void writeLoginToken(HttpServletResponse response,String sessionId){
        Cookie cookie = new Cookie(COOKIE_NAME, sessionId);
        cookie.setMaxAge(60 * 60 * 24 * 365);//1年
        //设置Cookie路径
        cookie.setPath("/");//表示webapp根目录下的所有地址都可以获得Cookie
        cookie.setDomain(COOKIE_DOMAIN);//表示.immoc.com的所有一级、二级、三级域名都可以获取Cookie

        cookie.setHttpOnly(true);
        log.info("wrtie -cookieName:{},cookieValue{}",cookie.getName(),cookie.getValue());
        response.addCookie(cookie);
    }

    //从request中读取Cookie中的JSESSIONID，用于判断权限
    public static String readLoginToken(HttpServletRequest request){
        Cookie[] cks = request.getCookies();
        if(cks != null){
            for(Cookie ck : cks){
                log.info("read  cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                if(StringUtils.equals(ck.getName(),COOKIE_NAME)){
                    log.info("return  cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    //删除Cookie中的JSESSIONID，用于注销登陆
    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cks = request.getCookies();
        if(cks != null){
            for(Cookie ck : cks){
                if(StringUtils.equals(ck.getName(),COOKIE_NAME)){
                    ck.setDomain(COOKIE_DOMAIN);
                    ck.setPath("/");
                    ck.setMaxAge(0);//设置成0，代表删除此cookie。
                    log.info("del cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                    response.addCookie(ck);
                    return;
                }
            }
        }
    }


}
