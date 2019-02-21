package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Created by geely
 */
public class PropertiesUtil {
//    将配置文件对象设置为私有的，外部通过自定义的方法获取value
    private static Properties props;
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    static {
        String filename = "mmall.properties";
        ClassLoader classLoader = PropertiesUtil.class.getClassLoader();
//        类加载器通过名字就能找到，不需要配置路径
        InputStream resourceAsStream = classLoader.getResourceAsStream(filename);
        try {
            //将从配置文件读取的字节流，用utf-8的编码方式转变为字符流，然后再放入配置文件对象props中，防止中文乱码
            InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream,"utf-8");
            /**
             * 一定要有
             */
            props = new Properties();
            props.load(inputStreamReader);
        } catch (IOException e) {
            logger.error("mmall.properties配置文件读取异常"+e.getMessage());
        }
    }
    //通过key获得value，value不存在，返回null
    public static String getProperty(String key){
        //避免properties的key和value前后有空格
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }
    //通过key获得value，value不存在，返回传入的默认值defaultValue
    public static String getProperty(String key,String defaultValue){
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            value = defaultValue;
        }
        return value.trim();
    }
}
