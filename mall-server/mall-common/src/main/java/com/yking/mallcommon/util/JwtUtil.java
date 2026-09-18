package com.yking.mallcommon.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 *
 * 负责 JWT Token 的生成和解析，用于用户身份认证：
 * - 生成 Token：用户登录成功后，生成包含 userId、username、role 的 JWT
 * - 解析 Token：从 Token 中提取用户信息
 * - 校验过期：判断 Token 是否已过期
 *
 * Token 结构（Payload 部分）：
 * {
 *   "sub": "admin",           // 用户名（subject）
 *   "userId": 1,              // 用户 ID
 *   "role": "ADMIN",          // 角色
 *   "iat": 1700000000,        // 签发时间
 *   "exp": 1700086400         // 过期时间
 * }
 *
 * 配置项（application.yml）：
 * - jwt.secret：签名密钥（Base64 编码，至少 256 位）
 * - jwt.expiration：过期时间（毫秒），默认 86400000 = 24 小时
 */
@Component
public class JwtUtil {

    /** JWT 签名密钥，从 application.yml 读取 */
    @Value("${jwt.secret:error}")
    private String secret;

    /** Token 过期时间（毫秒），默认 24 小时 */
    @Value("${jwt.expiration:86400000}")
    private long expiration;

    /**
     * 获取签名密钥
     *
     * 配置中的 jwt.secret 是 Base64 编码的 32 字节（256 位）随机值，
     * 必须先解码再作为 HMAC-SHA256 的密钥使用；
     * 若直接取字符串的 UTF-8 字节，实际密钥会与配置声明的格式不一致，
     * 日后按配置格式轮换密钥时会导致所有已签发令牌失效。
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户 ID（存入 claims）
     * @param username 用户名（作为 subject）
     * @param role     用户角色（存入 claims，如 "ADMIN" 或 "USER"）
     * @return JWT Token 字符串
     */
    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(username)                                            // 设置主题（用户名）
                .claims(Map.of("userId", userId, "role", role))              // 设置自定义声明
                .issuedAt(new Date())                                         // 设置签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 设置过期时间
                .signWith(getKey())                                           // 使用密钥签名
                .compact();                                                   // 生成最终 Token
    }

    /**
     * 解析 Token，获取 Payload（声明信息）
     *
     * @param token JWT Token 字符串
     * @return Claims 对象（包含 sub、userId、role 等信息）
     * @throws io.jsonwebtoken.JwtException Token 无效或已过期时抛出异常
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())           // 使用密钥验证签名
                .build()
                .parseSignedClaims(token)       // 解析 Token
                .getPayload();                   // 获取 Payload
    }

    /** 从 Token 中获取用户 ID */
    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /** 从 Token 中获取用户名 */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /** 从 Token 中获取用户角色 */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * 从 Token 中获取过期时间
     *
     * 登出吊销令牌时需要记录该时间，以便到期后清理吊销记录
     */
    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    /**
     * 判断 Token 是否已过期
     *
     * @param token JWT Token 字符串
     * @return true 表示已过期或 Token 无效，false 表示未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;  // 解析失败也视为过期
        }
    }
}
