package com.yking.malladmin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yking.malladmin.dto.UserVO;
import com.yking.malladmin.service.UserService;
import com.yking.mallcommon.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端用户管理接口
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户列表（分页）
     *
     * keyword 同时匹配用户名与昵称，为空时不过滤。
     * 返回结果不包含密码字段。
     */
    @GetMapping
    public Result<IPage<UserVO>> list(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword) {
        return Result.success(userService.page(page, size, keyword));
    }

    /**
     * 切换用户启用/禁用状态
     * 前端不传目标状态，由后端读取当前值取反
     */
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return Result.success();
    }
}
