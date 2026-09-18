package com.yking.mallcommon.util;

import org.springframework.security.core.Authentication;

/**
 * 认证信息工具
 *
 * JwtAuthFilter 解析 Token 后，把用户 ID 作为 principal 放进了 Authentication。
 * 控制器需要当前登录用户 ID 时统一通过本工具获取，
 * 避免各处自行强制转型而依赖 JwtAuthFilter 的实现细节。
 */
public final class AuthUtil {

    /** 令牌在请求头中的前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    private AuthUtil() {
    }

    /**
     * 获取当前登录用户 ID
     *
     * @param authentication 由 Spring Security 注入的认证信息
     * @return 当前登录用户 ID
     */
    public static Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }

    /**
     * 从 Authorization 请求头中取出令牌原文
     *
     * 登出接口需要令牌本身（而不是用户 ID），因此单独提供此方法，
     * 让 "Bearer " 这一格式约定只在一处定义
     *
     * @param authorizationHeader Authorization 请求头的值
     * @return 令牌原文；未携带令牌或格式不符时返回 null
     */
    public static String bearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
