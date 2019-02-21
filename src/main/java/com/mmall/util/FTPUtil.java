package com.mmall.util;


import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by tttppp606 on 2019/1/31.
 */
public class FTPUtil {
    private static  final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static FTPClient ftpClient = null;
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");
    private static String ftpDir = PropertiesUtil.getProperty("ftp.dir");

//  因为要用类直接调用方法，所以此处必须是静态方法！！！
    public static void upLoad(ArrayList<File> fileArrayList) throws IOException {
        logger.info("开始连接ftp服务器");
        upLoadDetail(ftpDir,fileArrayList);
        logger.info("结束上传");
    }

    private static void upLoadDetail(String remotePath,ArrayList<File> fileArrayList) throws IOException {
        //连接ftp服务器
        FileInputStream fis = null;

        connectServer(ftpIp,21,ftpUser,ftpPass);
        try {
            ftpClient.changeWorkingDirectory(remotePath);//设置存放上传文件的子目录
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);//二进制
            ftpClient.enterLocalPassiveMode();//被动方式与ftp的配置文件对应

            for (File file : fileArrayList) {
                fis = new FileInputStream(file);//创建一个文件输入流，读取file到流
                ftpClient.storeFile(file.getName(),fis);//storeFile(存放到ftp中的文件的名字,存放上传文件的输入流)
            }
        } catch (IOException e) {
            logger.error("上传文件异常"+e);
            e.printStackTrace();
        }finally {
            fis.close();//new的流要关闭
            ftpClient.disconnect();//ftpClient要关闭
        }
    }

    private static void connectServer(String ip,Integer port,String user,String pass){
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip, port);
            ftpClient.login(user,pass);
        } catch (IOException e) {
            logger.error("连接FTP服务器异常",e);
            e.printStackTrace();
        }
    }

}
