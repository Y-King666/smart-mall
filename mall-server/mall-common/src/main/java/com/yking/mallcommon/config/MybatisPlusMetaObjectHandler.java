package com.yking.mallcommon.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器
 *
 * 实体上标注 @TableField(fill = FieldFill.INSERT / INSERT_UPDATE) 的时间字段，
 * 在插入或更新时由本处理器自动填入当前时间。
 *
 * 未配置本处理器时的行为需要特别注意：MyBatis-Plus 依然会把这些 fill 字段拼进
 * INSERT 语句（以及逻辑删除的 UPDATE 语句），只是取值来自实体字段（通常为 null），
 * 于是显式写入的 NULL 会覆盖数据库的 DEFAULT CURRENT_TIMESTAMP —— 表现为新建记录的
 * 创建时间为空、逻辑删除把更新时间抹成 NULL。
 *
 * 填充策略中的 strict 前缀表示：只填充确实带有对应 fill 注解、且当前值为 null 的字段，
 * 因此不会覆盖业务代码显式赋的时间值。
 */
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    /** 创建时间字段名 */
    private static final String CREATE_TIME = "createTime";

    /** 更新时间字段名 */
    private static final String UPDATE_TIME = "updateTime";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        // 实体若没有对应字段（如 PmsCategory 没有 updateTime），strictFill 会自动跳过
        this.strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }
}
