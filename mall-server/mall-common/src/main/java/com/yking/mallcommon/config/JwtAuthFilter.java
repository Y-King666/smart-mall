package com.yking.mallcommon.config;

import com.yking.mallcommon.entity.SysUser;
import com.yking.mallcommon.mapper.SysUserMapper;
import com.yking.mallcommon.security.TokenBlacklist;
import com.yking.mallcommon.util.AuthUtil;
import com.yking.mallcommon.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器
 *
 * 继承 OncePerRequestFilter，确保每个请求只执行一次过滤逻辑。
 * 在 Spring Security 过滤器链中位于 UsernamePasswordAuthenticationFilter 之前。
 *
 * 工作流程：
 * 1. 从请求头中获取 Authorization 字段并取出令牌
 * 2. 令牌过期、无效或已被登出吊销 → 不建立认证
 * 3. 按令牌中的用户 ID 查询数据库，用户不存在（含已删除）或被禁用 → 不建立认证
 * 4. 以数据库中的角色构建权限，写入 SecurityContext
 *
 * 注意：
 * - 第 3、4 步刻意不直接采信令牌里的 role 声明：令牌一旦签发就无法修改，
 *   只信声明会导致「禁用用户」和「调整角色」在令牌过期前不生效
 * - Token 无效或过期时不抛异常，只是不设置认证（继续传递到后续过滤器），
 *   由 SecurityConfig 中的 URL 权限规则决定该请求是被放行还是拒绝
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /** 用户状态：启用 */
    private static final int STATUS_ENABLED = 1;

    /** Spring Security 要求的角色前缀 */
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtUtil jwtUtil;
    private final SysUserMapper sysUserMapper;
    private final TokenBlacklist tokenBlacklist;

    public JwtAuthFilter(JwtUtil jwtUtil, SysUserMapper sysUserMapper, TokenBlacklist tokenBlacklist) {
        this.jwtUtil = jwtUtil;
        this.sysUserMapper = sysUserMapper;
        this.tokenBlacklist = tokenBlacklist;
    }

    /**
     * 过滤器核心逻辑
     *
     * 每个 HTTP 请求都会经过此方法：
     * - 令牌合法且对应用户可用：设置认证上下文
     * - 其余情况：不设置认证（匿名访问，由 SecurityConfig 决定是否放行）
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = AuthUtil.bearerToken(request.getHeader("Authorization"));

        if (token != null) {
            try {
                authenticateIfValid(token);
            } catch (Exception ignored) {
                // Token 解析失败（格式错误、签名不匹配等），不设置认证，继续执行过滤器链
                // SecurityConfig 的 URL 规则会决定该请求是被放行还是拒绝
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 令牌通过全部校验时建立认证信息
     *
     * 任一校验不通过都直接返回（等同于未登录），由 SecurityConfig 拒绝该请求
     */
    private void authenticateIfValid(String token) {
        // 1. 令牌必须未过期，且未在登出时被吊销
        if (jwtUtil.isTokenExpired(token) || tokenBlacklist.isRevoked(token)) {
            return;
        }

        // 2. 以数据库记录为准校验账号可用性
        //    逻辑删除条件由 MyBatis-Plus 全局配置自动追加，已删除用户查不出来
        SysUser user = sysUserMapper.selectById(jwtUtil.getUserId(token));
        if (user == null || user.getStatus() == null || user.getStatus() != STATUS_ENABLED) {
            return;
        }

        // 3. 使用数据库中的角色，使角色变更立即生效
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole())
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);
        auth.setDetails(user.getUsername());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
