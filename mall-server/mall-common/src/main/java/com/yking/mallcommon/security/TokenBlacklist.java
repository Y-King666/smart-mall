package com.yking.mallcommon.security;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 已吊销令牌集合
 *
 * JWT 是无状态的，令牌一经签发就在过期前始终有效，服务端无法主动"收回"。
 * 用户登出时把令牌登记到本集合，JwtAuthFilter 每次请求都会检查，
 * 从而让该令牌立即失效。
 *
 * 实现为进程内存储：服务重启后集合清空，此前登出的令牌会恢复可用，直到其自然过期。
 * 项目未引入 Redis，单实例部署下这个取舍可以接受；若将来做多实例部署，
 * 应把本集合替换为 Redis 等共享存储。
 */
@Component
public class TokenBlacklist {

    /** 令牌原文 → 该令牌自身的过期时间（毫秒时间戳），用于判断条目何时可以清理 */
    private final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();

    /**
     * 吊销令牌
     *
     * @param token      令牌原文
     * @param expiration 令牌自身的过期时间，到期后该条目无需继续保留
     */
    public void revoke(String token, Date expiration) {
        purgeExpired();
        revokedTokens.put(token, expiration.getTime());
    }

    /** 判断令牌是否已被吊销 */
    public boolean isRevoked(String token) {
        return revokedTokens.containsKey(token);
    }

    /** 清理已自然过期的条目，避免集合随登出次数无限增长 */
    private void purgeExpired() {
        long now = System.currentTimeMillis();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
