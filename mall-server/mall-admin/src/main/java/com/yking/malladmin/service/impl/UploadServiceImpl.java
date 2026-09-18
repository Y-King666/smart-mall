package com.yking.malladmin.service.impl;

import com.yking.malladmin.service.UploadService;
import com.yking.mallcommon.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    /**
     * 允许的图片类型 → 存储扩展名
     *
     * 扩展名由白名单决定，不采用客户端上传的文件名，
     * 避免伪造扩展名带来的安全风险
     */
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/gif", "gif",
            "image/webp", "webp");

    /** 图片存储子目录 */
    private static final String IMAGE_SUB_DIR = "images";

    /** 图片访问路径前缀，与 WebMvcConfig 的静态资源映射、SecurityConfig 的放行规则保持一致 */
    private static final String IMAGE_URL_PREFIX = "/api/uploads/images/";

    /** 上传根目录的绝对路径 */
    private final Path uploadRoot;

    public UploadServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        // 部分客户端不发送 Content-Type，而 Map.of 创建的不可变 Map 在 get(null) 时会抛 NPE
        String contentType = file.getContentType();
        String extension = contentType == null ? null : ALLOWED_TYPES.get(contentType);
        if (extension == null) {
            throw new BusinessException("仅支持 jpg、png、gif、webp 格式的图片");
        }

        // 使用 UUID 作为文件名，避免同名覆盖与中文文件名带来的编码问题
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path imageDir = uploadRoot.resolve(IMAGE_SUB_DIR);
        try {
            Files.createDirectories(imageDir);
            file.transferTo(imageDir.resolve(fileName));
        } catch (IOException e) {
            log.error("图片上传失败，目标目录：{}", imageDir, e);
            throw new BusinessException("图片上传失败");
        }
        return IMAGE_URL_PREFIX + fileName;
    }
}
