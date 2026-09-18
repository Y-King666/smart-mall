package com.yking.malladmin.service;

import com.yking.malladmin.dto.LoginRequest;
import com.yking.malladmin.dto.LoginResponse;
import com.yking.malladmin.dto.RegisterRequest;
import com.yking.malladmin.dto.UserInfoResponse;

public interface AuthService {
    /**
     * 登录业务逻辑处理
     * @param loginRequest
     * @return
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 注册业务逻辑处理
     * @param registerRequest
     */
    void register(RegisterRequest registerRequest);

    /**
     * 获取当前登录用户信息
     * @param userId
     * @return
     */
    UserInfoResponse getCurrentUser(Long userId);

    /**
     * 退出登录：吊销当前令牌，使其立即失效
     * @param token 令牌原文，未携带令牌时为 null
     */
    void logout(String token);
}
