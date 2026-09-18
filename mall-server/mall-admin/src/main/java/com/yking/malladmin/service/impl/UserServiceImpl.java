package com.yking.malladmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yking.malladmin.dto.UserVO;
import com.yking.malladmin.service.UserService;
import com.yking.mallcommon.entity.SysUser;
import com.yking.mallcommon.exception.BusinessException;
import com.yking.mallcommon.mapper.SysUserMapper;
import com.yking.mallcommon.util.DateTimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private static final String ROLE_ADMIN = "ADMIN";

    /** 状态：启用 */
    private static final int STATUS_ENABLED = 1;

    private final SysUserMapper sysUserMapper;

    public UserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public IPage<UserVO> page(long page, long size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                // 关键字同时匹配用户名与昵称，用 and 包住 or 条件，避免影响其他查询条件
                .and(StringUtils.hasText(keyword), condition -> condition
                        .like(SysUser::getUsername, keyword)
                        .or()
                        .like(SysUser::getNickname, keyword))
                // 按 id 倒序，保证分页结果稳定
                .orderByDesc(SysUser::getId);
        return sysUserMapper.selectPage(new Page<>(page, size), wrapper).convert(this::toVO);
    }

    @Override
    public void toggleStatus(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 登录时只校验 status，禁用管理员会把管理员挡在系统之外（含操作者本人）
        if (ROLE_ADMIN.equals(user.getRole()) && user.getStatus() != null && user.getStatus() == STATUS_ENABLED) {
            throw new BusinessException("不能禁用管理员账号");
        }

        // 直接在 SQL 中取反，避免「查询-修改-写回」之间的竞态
        int rows = sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .setSql("status = 1 - status"));
        if (rows == 0) {
            throw new BusinessException("用户不存在");
        }
    }

    /** 转换为返回给前端的行数据，password 不参与映射 */
    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(DateTimeUtil.format(user.getCreateTime()));
        return vo;
    }
}
