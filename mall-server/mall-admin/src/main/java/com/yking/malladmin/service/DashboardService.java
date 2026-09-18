package com.yking.malladmin.service;

import com.yking.malladmin.dto.DashboardVO;

/**
 * 仪表盘业务逻辑
 */
public interface DashboardService {

    /**
     * 获取仪表盘统计数据（统计卡片 + 近 7 天销售趋势）
     */
    DashboardVO getDashboard();
}
