package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.model.AiApplication;
import com.xiaodou.mapper.AiApplicationMapper;
import com.xiaodou.service.AiApplicationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * AI 应用配置主表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Service
public class AiApplicationServiceImpl extends ServiceImpl<AiApplicationMapper, AiApplication>
    implements AiApplicationService {
    @Override
    public Page<AiApplication> pageApplications(Integer current, Integer size, String name, String typeId,
        Byte enabled) {
        LambdaQueryWrapper<AiApplication> query = new LambdaQueryWrapper<>();

        if (name != null && !name.trim()
            .isEmpty()) {
            query.like(AiApplication::getName, name.trim());
        }
        if (typeId != null && !typeId.trim()
            .isEmpty()) {
            query.eq(AiApplication::getTypeId, typeId.trim());
        }
        if (enabled != null) {
            query.eq(AiApplication::getEnabled, enabled);
        }

        query.orderByDesc(AiApplication::getUpdatedAt);

        return this.page(new Page<>(current, size), query);
    }
}
