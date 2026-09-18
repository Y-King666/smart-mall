package com.yking.mallcommon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Spring MVC 配置
 *
 * 把本地上传目录映射为静态资源路径，使上传后的图片可以通过 URL 直接访问。
 * 访问前缀 /api/uploads/** 需与以下两处保持一致：
 * - SecurityConfig 中对该路径的匿名放行规则（img 标签不会携带 JWT，必须免认证）
 * - UploadService 返回给前端的图片 URL 前缀
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 上传文件的访问路径前缀 */
    private static final String UPLOAD_URL_PATTERN = "/api/uploads/**";

    /** 上传目录的 file: URI 形式，必须以 / 结尾才会被当作目录 */
    private final String uploadLocation;

    public WebMvcConfig(@Value("${file.upload-dir}") String uploadDir) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        this.uploadLocation = location.endsWith("/") ? location : location + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(UPLOAD_URL_PATTERN).addResourceLocations(uploadLocation);
    }
}
