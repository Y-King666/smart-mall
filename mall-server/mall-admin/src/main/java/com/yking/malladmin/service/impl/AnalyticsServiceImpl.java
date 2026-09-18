package com.yking.malladmin.service.impl;

import com.yking.malladmin.dto.AnalyticsHealthVO;
import com.yking.malladmin.service.AnalyticsService;
import com.yking.mallcommon.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * 数据分析服务转发实现
 *
 * Python 服务的响应本身也是 {code, message, data} 结构，
 * 这里只取出其中的 data 部分，再由 Controller 包进本项目的 Result，
 * 保证前端只需解包一次即可拿到数据。
 */
@Slf4j
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final RestClient restClient;

    private final String baseUrl;

    public AnalyticsServiceImpl(@Value("${analytics.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public Map<String, Object> sales(Integer days) {
        return getData("/api/analytics/sales?days={days}", days);
    }

    @Override
    public Map<String, Object> users() {
        return getData("/api/analytics/users");
    }

    @Override
    public Map<String, Object> recommend(Long categoryId, Integer limit) {
        // 前端参数名为 categoryId，Python 服务使用的是 category_id
        if (categoryId == null) {
            return getData("/api/analytics/recommend?limit={limit}", limit);
        }
        return getData("/api/analytics/recommend?category_id={categoryId}&limit={limit}", categoryId, limit);
    }

    @Override
    public Map<String, Object> userRecommend(Long userId, Integer limit) {
        return getData("/api/analytics/recommend/user/{userId}?limit={limit}", userId, limit);
    }

    @Override
    public AnalyticsHealthVO health() {
        try {
            restClient.get().uri("/api/health").retrieve().toBodilessEntity();
            return new AnalyticsHealthVO(true, "数据分析服务连接正常");
        } catch (RestClientException e) {
            log.error("数据分析服务健康检查失败：{}", baseUrl, e);
            return new AnalyticsHealthVO(false, "数据分析服务不可用，请确认 mall-analytics 已启动（" + baseUrl + "）");
        }
    }

    /**
     * 调用 Python 接口并返回其响应中的 data 部分
     *
     * @param uriTemplate  Python 接口路径（含占位符）
     * @param uriVariables 占位符对应的值
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getData(String uriTemplate, Object... uriVariables) {
        Map<String, Object> response;
        try {
            response = restClient.get().uri(uriTemplate, uriVariables)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            log.error("调用数据分析服务失败：{}{}", baseUrl, uriTemplate, e);
            throw new BusinessException("数据分析服务不可用，请确认 mall-analytics 已启动（" + baseUrl + "）");
        }

        // Python 侧捕获异常时会返回 code=500 且 data 为 null
        Object data = response == null ? null : response.get("data");
        if (data == null) {
            String message = response == null ? "服务无响应" : String.valueOf(response.get("message"));
            throw new BusinessException("数据分析失败：" + message);
        }
        return (Map<String, Object>) data;
    }
}
