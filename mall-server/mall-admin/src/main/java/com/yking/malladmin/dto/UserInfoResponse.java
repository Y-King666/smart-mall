package com.yking.malladmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户信息响应
 *
 * 与 LoginResponse 的字段保持一致（仅不含 token），
 * 前端把它整体存入 localStorage 的 userInfo，并从中读取 nickname 展示在顶栏。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 角色：ADMIN / USER */
    private String role;
}
