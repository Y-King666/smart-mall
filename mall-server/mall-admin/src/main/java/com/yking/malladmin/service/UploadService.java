package com.yking.malladmin.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传业务逻辑
 */
public interface UploadService {

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return 可直接访问的图片 URL
     */
    String uploadImage(MultipartFile file);
}
