package com.yking.malladmin.controller;

import com.yking.malladmin.dto.DashboardVO;
import com.yking.malladmin.service.DashboardService;
import com.yking.mallcommon.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 仪表盘统计数据：统计卡片 + 近 7 天销售趋势
     */
    @GetMapping
    public Result<DashboardVO> getDashboardData() {
        return Result.success(dashboardService.getDashboard());
    }
}
