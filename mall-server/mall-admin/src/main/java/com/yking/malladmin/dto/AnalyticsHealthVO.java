package com.yking.malladmin.dto;

/**
 * 数据分析服务健康状态
 *
 * 前端根据 pythonServiceHealthy 决定是否弹出服务状态提示框，
 * message 作为提示框中的说明文本。服务不可用时仍返回成功响应，
 * 只把该字段置为 false，以便前端走提示逻辑而不是报错逻辑。
 *
 * @param pythonServiceHealthy Python 数据分析服务是否可用
 * @param message               状态说明
 */
public record AnalyticsHealthVO(boolean pythonServiceHealthy, String message) {
}
