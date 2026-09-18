package com.yking.malladmin.service;

import com.yking.malladmin.dto.AnalyticsHealthVO;

import java.util.Map;

/**
 * 数据分析业务逻辑
 *
 * 分析计算全部由 Python 数据分析服务（mall-analytics）完成，
 * 本服务只负责转发请求并把结果返回给前端。
 */
public interface AnalyticsService {

    /** 销售统计分析（汇总、Top10 商品、分类统计、每日趋势） */
    Map<String, Object> sales(Integer days);

    /** 用户价值分析（用户分类统计、Top20 高价值用户） */
    Map<String, Object> users();

    /** 商品推荐（分类热门或全局热门） */
    Map<String, Object> recommend(Long categoryId, Integer limit);

    /** 指定用户的个性化商品推荐 */
    Map<String, Object> userRecommend(Long userId, Integer limit);

    /** 数据分析服务健康状态 */
    AnalyticsHealthVO health();
}
