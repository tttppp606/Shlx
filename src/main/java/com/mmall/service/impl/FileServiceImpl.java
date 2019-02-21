package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by tttppp606 on 2019/1/31.
 */
@Service
public class FileServiceImpl implements IFileService {
    Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public ServerResponse upload(MultipartFile file, String path) {
        //1、根据上传文件的名字，起新名字
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        String uploadFileName = UUID.randomUUID().toString()+suffix;

        //2、根据控制层传来的项目根路径，建立一个文件（需要路径+文件名）
        File fileDir = new File(path);
        if (!fileDir.exists()){//判断文件是否存在，用exists()方法
            fileDir.setWritable(true);//针对centos系统，没有在tomcat上创建文件的权限
            fileDir.mkdirs();
        }
        File targetFile = new File(path, uploadFileName);
        //3、将上传文件导入新建立的目标文件
        try {
            file.transferTo(targetFile);
            logger.info("文件上传，上传时间：{}，上传文件名：{}，上传路径：{}，上传后文件名：{}",new Date(),fileName,path,uploadFileName);
         // 4、将文件再上传到ftp服务器（也可以直接传到ftp服务器）
            //将存有上传文件的新文件放入一个文件List中
            ArrayList<File> fileArrayList = new ArrayList<>();
            fileArrayList.add(targetFile);
            /**
             * 输入：远程ftp服务器里存放上传文件的文件夹下的一个文件夹名字，文件list
             * 返回：上传成功就正常返回，不成功，在FtpUtil会抛出异常，这里捕获，输入日志不正常
             */
             FTPUtil.upLoad(fileArrayList);
//             删除临时文件
             targetFile.delete();
        } catch (IOException e) {
            logger.error("文件上传失败",e);
            e.printStackTrace();
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("uri",targetFile.getName());
        String prefix = PropertiesUtil.getProperty("ftp.server.http.prefix");
        map.put("url",prefix+targetFile.getName());

        return ServerResponse.createBySuccess(map);
    }
}
