package com.yking.malladmin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yking.malladmin.dto.UserVO;

/**
 * 用户管理业务逻辑
 */
public interface UserService {

    /**
     * 分页查询用户
     *
     * @param keyword 用户名或昵称模糊关键字，为空则不过滤
     */
    IPage<UserVO> page(long page, long size, String keyword);

    /** 切换用户启用/禁用状态 */
    void toggleStatus(Long id);
}
