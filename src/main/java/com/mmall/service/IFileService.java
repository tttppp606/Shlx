package com.mmall.service;

import com.mmall.common.ServerResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by tttppp606 on 2019/1/31.
 */
public interface IFileService {
    ServerResponse upload(MultipartFile file, String path);
}
