package com.xiaodou.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MyBatis-Plus 元对象处理器
 * <p>
 * 实现自动填充功能，用于处理实体类中需要自动填充的字段：
 * 1. 插入操作时自动填充 createdAt(创建时间) 和 updatedAt(更新时间)
 * 2. 更新操作时自动填充 updatedAt(更新时间)
 * </p>
 *
 * @author xiaodou V=>dddou117
 * @since  2025/5/10
 * @version V1.0
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作自动填充方法
     * <p>
     * 在执行INSERT操作时自动填充以下字段：
     * 1. createdAt - 使用当前时间（上海时区）
     * 2. updatedAt - 使用当前时间（上海时区）
     * </p>
     *
     * @param metaObject 元对象，包含实体类的属性信息
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 严格模式填充创建时间字段
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class,
            LocalDateTime.now(ZoneId.of("Asia/Shanghai")));

        // 严格模式填充更新时间字段
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class,
            LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
    }

    /**
     * 更新操作自动填充方法
     * <p>
     * 在执行UPDATE操作时自动填充以下字段：
     * 1. updatedAt - 使用当前时间（上海时区）
     * </p>
     *
     * @param metaObject 元对象，包含实体类的属性信息
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 严格模式填充更新时间字段
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class,
            LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
    }
}
