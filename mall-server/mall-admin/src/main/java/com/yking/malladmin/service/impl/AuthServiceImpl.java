package com.yking.malladmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yking.malladmin.dto.LoginRequest;
import com.yking.malladmin.dto.LoginResponse;
import com.yking.malladmin.dto.RegisterRequest;
import com.yking.malladmin.dto.UserInfoResponse;
import com.yking.malladmin.service.AuthService;
import com.yking.mallcommon.entity.SysUser;
import com.yking.mallcommon.mapper.SysUserMapper;
import com.yking.mallcommon.security.TokenBlacklist;
import com.yking.mallcommon.util.JwtUtil;
import com.yking.mallcommon.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /** 注册接口对外公开，统一赋予普通用户角色 */
    private static final String ROLE_USER = "USER";

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;

    public AuthServiceImpl(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                           TokenBlacklist tokenBlacklist) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 1. 根据用户名到数据库查询用户
        //    password 字段标注了 select = false，校验密码必须显式查询该列
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .select(SysUser::getId, SysUser::getUsername, SysUser::getPassword,
                                SysUser::getNickname, SysUser::getAvatar, SysUser::getRole, SysUser::getStatus)
                        .eq(SysUser::getUsername, loginRequest.getUsername())
        );
        // 2.校验查询的用户是否存在
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 3.校验用户密码是否一致
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException("密码不一致");
        }
        // 4.校验用户是否被禁用
        if (user.getStatus() != 1) {
            throw new BusinessException("用户被禁用");
        }
        // 5.用户匹配上并合法，生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        //  6.构建返回登录响应数据
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        // 校验用户名唯一
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, registerRequest.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(registerRequest.getUsername());
        // 密码使用 BCrypt 加密后存储，与登录时的校验方式对应
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setNickname(StringUtils.hasText(registerRequest.getNickname())
                ? registerRequest.getNickname()
                : registerRequest.getUsername());
        user.setRole(ROLE_USER);
        // 状态、逻辑删除标记由数据库默认值填充
        sysUserMapper.insert(user);
    }

    @Override
    public UserInfoResponse getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }

    @Override
    public void logout(String token) {
        if (token == null) {
            return;
        }
        try {
            // 记录令牌自身的过期时间，到期后即可把该条目从吊销集合中清理掉
            tokenBlacklist.revoke(token, jwtUtil.getExpiration(token));
        } catch (Exception e) {
            // 令牌本身已损坏或已过期，无需吊销；登出对用户而言仍然成功
            log.warn("退出登录时令牌解析失败：{}", e.getMessage());
        }
    }
}
