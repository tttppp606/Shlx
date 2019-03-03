package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by tttppp606 on 2019/1/26.
 */
public class TokenCache {
    private static Logger logger= LoggerFactory.getLogger(TokenCache.class);
    public static final String TOKEN_PREFIX = "token_";

    private static LoadingCache<String,String> localCache = CacheBuilder
            .newBuilder()
            .initialCapacity(1000)//设置缓存容器的初始容量
            .maximumSize(10000)//设置缓存最大容量，超过之后就会按照使用LRU算法来移除缓存项
            .expireAfterAccess(12, TimeUnit.HOURS)//设置缓冲12小时后过期
            //默认的数据加载实现,当调用get取值的时候,如果key没有对应的值,就调用这个方法进行加载.
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    //防止空指针
                    return "null";
                }
            });

    public static void setKey(String key,String value){
        localCache.put(key,value);
    }

    public static String getValue(String key){
        String value = null;
        try {
            value=localCache.get(key);
            if ("null".equals(value)){
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            //输出error类型的日志
            logger.error("localCache get error",e);
            e.printStackTrace();
        }
        return null;
    }
}
