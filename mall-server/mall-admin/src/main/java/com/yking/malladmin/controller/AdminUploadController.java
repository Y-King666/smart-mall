package com.yking.malladmin.controller;

import com.yking.malladmin.service.UploadService;
import com.yking.mallcommon.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理端文件上传接口
 */
@RestController
@RequestMapping("/api/admin/upload")
public class AdminUploadController {

    private final UploadService uploadService;

    public AdminUploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    /**
     * 上传图片
     *
     * 表单字段名固定为 file（与前端 FormData 中的字段名一致），
     * 返回的 data 直接是图片 URL 字符串，前端会将其赋给商品的 coverImage
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(uploadService.uploadImage(file));
    }
}
