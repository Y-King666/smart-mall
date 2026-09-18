package com.yking.malladmin.dto;

import lombok.Data;

/**
 * 用户列表行数据
 *
 * 单独定义而非直接返回 SysUser 实体的原因：
 * 1. 实体带有 password 字段，直接序列化会把密码哈希泄露给前端
 * 2. createTime 在实体中为 java.util.Date，需按 yyyy-MM-dd HH:mm:ss 输出，
 *    前端表格直接展示字符串、不做任何格式化
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    /** 角色：ADMIN / USER */
    private String role;

    /** 状态：1=启用，0=禁用 */
    private Integer status;

    /** 注册时间，格式 yyyy-MM-dd HH:mm:ss */
    private String createTime;
}
