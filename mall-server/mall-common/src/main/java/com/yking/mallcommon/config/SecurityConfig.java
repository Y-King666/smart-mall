package com.yking.mallcommon.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.DispatcherType;

import java.util.List;

/**
 * Spring Security 安全配置类
 *
 * 主要配置三项内容：
 * 1. URL 访问权限规则（哪些接口公开、哪些需要登录、哪些需要管理员角色）
 * 2. CORS 跨域配置（允许前端 localhost:3000/3001 跨域请求后端）
 * 3. 密码编码器（BCrypt 加密）
 *
 * 认证机制：
 * - 无状态会话（STATELESS）：不依赖 Session，使用 JWT Token 认证
 * - JwtAuthFilter 在 UsernamePasswordAuthenticationFilter 之前执行
 *   从请求头 Authorization: Bearer <token> 中解析用户信息
 *
 * 注意事项：
 * - 修改 URL 规则后需要重启后端服务才能生效
 * - /api/uploads/** 是上传的图片静态资源路径，需允许匿名访问
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 启用方法级别权限控制（如 @PreAuthorize）
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * 配置安全过滤链
     *
     * 规则说明（按顺序匹配）：
     * - /api/auth/login、/api/auth/register → 完全公开（登录注册不需要 Token）
     * - /api/auth/logout → 完全公开（吊销自己已持有的令牌不需要额外权限；
     *   若要求登录，令牌一过期用户就无法正常登出，只能收到 403）
     * - /api/portal/products/**、/api/portal/categories/** → 完全公开（前台商品浏览）
     * - /api/uploads/** → 完全公开（上传图片的静态资源访问）
     * - /api/admin/** → 需要 ADMIN 角色（后台管理接口）
     * - 其他所有请求 → 需要认证（携带有效 Token）
     */
    @PostConstruct
    public void init() {
        log.info("==========================依赖注入完成================================");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)                                    // 禁用 CSRF跨站请求伪造（前后端分离项目不需要）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))         // 启用 CORS 跨域支持
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无状态会话
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()              // 异步派发（SSE等）：放行，避免 Security 二次鉴权报错
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/logout").permitAll()  // 登录注册登出：公开
                        .requestMatchers("/api/portal/products/**", "/api/portal/categories/**").permitAll() // 前台商品：公开
                        .requestMatchers("/api/uploads/**").permitAll()                        // 静态资源：公开
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")                     // 后台管理：需要 ADMIN 角色
                        .anyRequest().authenticated()                                          // 其他：需要认证
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // JWT 过滤器在认证过滤器之前执行
        return http.build();
    }

    /**
     * CORS 跨域配置
     *
     * 允许前端开发服务器（localhost:3000 mall-admin、localhost:3001 mall-portal）
     * 跨域请求后端（localhost:8080）
     *
     * 生产环境应将 allowedOriginPatterns 改为实际的前端域名
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));                              // 允许所有来源（所有 IP / 域名http://localhost:*）
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 允许的 HTTP 方法
        config.setAllowedHeaders(List.of("*"));                                       // 允许所有请求头
        config.setAllowCredentials(true);                                             // 允许携带 Cookie/认证信息
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);                              // 所有路径都应用此配置
        return source;
    }

    /**
     * 密码编码器
     *
     * 使用 BCrypt 算法加密密码（单向哈希，不可逆）
     * 注册为用户后，密码用此编码器加密后存入数据库
     * 登录时，用此编码器验证用户输入的密码是否匹配
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
