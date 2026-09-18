package com.yking.malladmin.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder  // 支持链式调用
@NoArgsConstructor  // 生成无参构造函数
@AllArgsConstructor  // 生成全参构造函数

public class LoginResponse {

    /**
     * 访问令牌
     */
    private String token;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 角色
     */
    private String role;
}
