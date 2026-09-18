package com.yking.malladmin.controller;

import com.yking.malladmin.dto.LoginResponse;
import com.yking.malladmin.dto.RegisterRequest;
import com.yking.malladmin.dto.UserInfoResponse;
import com.yking.malladmin.service.AuthService;
import jakarta.validation.Valid;
import com.yking.mallcommon.Result;
import com.yking.mallcommon.util.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.yking.malladmin.dto.LoginRequest;


/**
 * 认证中心控制器
 */

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    /**
     * 登录接口
     * @Valid 启用参数校验
     * @RequestBody 将请求体中的 JSON 数据绑定到 loginRequest 参数
     * @param loginRequest
     * @return
     */

    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest loginRequest){
        // 只记录用户名；密码属敏感信息，不得写入日志
        log.info("用户登录：{}", loginRequest.getUsername());
        LoginResponse loginResponse = authService.login(loginRequest);
        return Result.success(loginResponse);
    }

    /**
     * 注册接口
     * 注册成功的账号统一为普通用户（USER）角色
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     * 用户 ID 由 JwtAuthFilter 解析 Token 后放入 Authentication 的 principal
     */
    @GetMapping("/info")
    public Result<UserInfoResponse> info(Authentication authentication) {
        return Result.success(authService.getCurrentUser(AuthUtil.currentUserId(authentication)));
    }

    /**
     * 退出登录
     * 把当前令牌加入吊销集合使其立即失效（JWT 无法主动作废，需服务端记录）
     * 接口本身不要求登录：吊销一个自己已持有的令牌不需要额外权限，也避免令牌已过期时登出报错
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(AuthUtil.bearerToken(authorization));
        return Result.success();
    }
}