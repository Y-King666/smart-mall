package com.yking.malladmin.controller;

import com.yking.malladmin.dto.AnalyticsHealthVO;
import com.yking.malladmin.service.AnalyticsService;
import com.yking.mallcommon.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端数据分析接口
 *
 * 分析计算由 Python 数据分析服务完成，本控制器只做转发，
 * 返回的 JSON 字段名（snake_case 及中文分类名）与 Python 服务保持一致，
 * 前端 analytics 页面直接按这些字段名读取。
 */
@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    public AdminAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /** 销售统计分析 */
    @GetMapping("/sales")
    public Result<Map<String, Object>> sales(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(analyticsService.sales(days));
    }

    /** 用户价值分析 */
    @GetMapping("/users")
    public Result<Map<String, Object>> users() {
        return Result.success(analyticsService.users());
    }

    /** 商品推荐（categoryId 为空时返回全局热门商品） */
    @GetMapping("/recommend")
    public Result<Map<String, Object>> recommend(@RequestParam(required = false) Long categoryId,
                                                 @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(analyticsService.recommend(categoryId, limit));
    }

    /** 指定用户的个性化商品推荐 */
    @GetMapping("/recommend/user/{userId}")
    public Result<Map<String, Object>> userRecommend(@PathVariable Long userId,
                                                     @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(analyticsService.userRecommend(userId, limit));
    }

    /**
     * 数据分析服务健康状态
     * 服务不可用时仍返回成功响应，前端据 pythonServiceHealthy 弹出提示
     */
    @GetMapping("/health")
    public Result<AnalyticsHealthVO> health() {
        return Result.success(analyticsService.health());
    }
}
