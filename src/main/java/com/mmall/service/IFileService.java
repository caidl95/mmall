package com.mmall.service;

import com.mmall.common.ServerResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {

    /**
     * 上传文件
     */
    String upload(MultipartFile file, String path);

    /**
    * 校验上传文件大小
     *
     */
    ServerResponse getFileSize(MultipartFile file);

    /**
     * 效验上传的文件是否为图片
     */
    ServerResponse getFileTypeIsIMG(MultipartFile file);

    /**
     * 效验上传文件的类型和图片
     */
    ServerResponse getFileTypeAndSize(MultipartFile file);
}
