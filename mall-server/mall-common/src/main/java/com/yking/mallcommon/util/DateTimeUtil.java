package com.yking.mallcommon.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时间格式化工具
 *
 * 项目未统一配置 Jackson 的时间格式（Boot 4 使用的 Jackson 3 与旧版配置方式不同），
 * 而前端页面直接展示后端返回的时间字符串、不做任何格式化，
 * 因此由各查询接口在组装返回数据时统一格式化为 yyyy-MM-dd HH:mm:ss。
 */
public final class DateTimeUtil {

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN);

    private DateTimeUtil() {
    }

    /**
     * 格式化 LocalDateTime（商品、订单等实体的时间字段）
     *
     * @return 入参为 null 时返回空串，避免前端展示 null
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * 格式化 java.util.Date（SysUser 的时间字段为该类型）
     *
     * SimpleDateFormat 非线程安全，因此每次调用新建实例
     */
    public static String format(Date date) {
        return date == null ? "" : new SimpleDateFormat(PATTERN).format(date);
    }
}
