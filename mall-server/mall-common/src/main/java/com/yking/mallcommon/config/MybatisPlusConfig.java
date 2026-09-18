package com.yking.mallcommon.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置
 *
 * 注册分页插件。Mapper 使用 Page 作为查询参数时，插件负责：
 * 1. 自动执行一次 COUNT 查询，把总数写入 Page.total
 * 2. 在 SQL 末尾拼接 LIMIT，只取当前页数据
 *
 * 未注册该插件时 Page 不会生效：查询会返回全表数据，且 total 恒为 0，
 * 前端分页组件将无法工作。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 指定数据库类型，分页插件据此生成对应方言的 LIMIT 语句
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
