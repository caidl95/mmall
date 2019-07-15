package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.common.ServerResponse;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     * 确定上传文件的最大大小
     */
    private static final long UPLOAD_MAX_SIZE = 1 * 1024 * 1024;

    /**
     * 确定允许上传的类型的列表
     */
    private static final List<String> UPLOAD_CONTENT_TYPES
            = new ArrayList<>();

    static {
        UPLOAD_CONTENT_TYPES.add("image/jpeg");
        UPLOAD_CONTENT_TYPES.add("image/png");
        UPLOAD_CONTENT_TYPES.add("image/gif");
        UPLOAD_CONTENT_TYPES.add("image/bmp");
    }


    public String upload(MultipartFile file,String path){

        if (file.isEmpty()) {// 检查文件是否为空
            // 为空返回null
            return null;
        }

        String fileName = file.getOriginalFilename();
        //扩展名  +1  去掉点
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID()+"."+fileExtensionName;
        logger.info("开始上传文件，上传的文件名:{}，上传的路径:{}，新文件名:{}",fileName,path,uploadFileName);

        File fileDir = new File(path);
        if (!fileDir.exists()){//判断是否存在
            fileDir.setWritable(true);//给予权限
            fileDir.mkdirs();//不存在就创建新的
        }

        File targetFile = new File(path,uploadFileName);
        try{
            file.transferTo(targetFile);//文件上传成功

            //将targetFile上传到我们的FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //上传完之后，删除upload下面的文件
            targetFile.delete();
        } catch (IOException e){
            logger.error("上传文件异常");
            return null;
        }
        return targetFile.getName();
    }

    public ServerResponse getFileSize(MultipartFile file){
        // 检查文件大小
        if (file.getSize() > UPLOAD_MAX_SIZE) {
            // 超出范围(> UPLOAD_MAX_SIZE)：抛出异常：FileSizeException
            return ServerResponse.createByErrorMessage("上传头像错误！不允许上传超过" + (UPLOAD_MAX_SIZE / 1024) + "KB的文件！");
        }
       return ServerResponse.createBySuccess();
    }

    public ServerResponse getFileTypeIsIMG(MultipartFile file){
        // 检查文件类型
        String contentType = file.getContentType();
        if (!UPLOAD_CONTENT_TYPES.contains(contentType)) {
            // 类型不符(contains()为false)
            return ServerResponse.createByErrorMessage("上传图片错误！不支持选择的文件类型！");
        }
        return ServerResponse.createBySuccess();
    }

    public ServerResponse getFileTypeAndSize(MultipartFile file){
        // 检查文件大小
        if (file.getSize() > UPLOAD_MAX_SIZE) {
            // 超出范围(> UPLOAD_MAX_SIZE)：抛出异常：FileSizeException
            return ServerResponse.createByErrorMessage("上传头像错误！不允许上传超过" + (UPLOAD_MAX_SIZE / 1024) + "KB的文件！");
        }
        // 检查文件类型
        String contentType = file.getContentType();
        if (!UPLOAD_CONTENT_TYPES.contains(contentType)) {
            // 类型不符(contains()为false)
            return ServerResponse.createByErrorMessage("上传图片错误！不支持选择的文件类型！");
        }
        return ServerResponse.createBySuccess();
    }



}
