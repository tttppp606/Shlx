package com.mmall.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by tttppp606 on 2019/1/30.
 */
public class properties {
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        FileInputStream fileInputStream = new FileInputStream("mmall.properties");
        properties.load(fileInputStream);
        String property = properties.getProperty("ftp.pass");
        System.out.println(property);

    }
}
