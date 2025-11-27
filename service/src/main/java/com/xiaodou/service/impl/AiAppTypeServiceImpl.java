package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.exception.AppException;
import com.xiaodou.mapper.AiAppTypeMapper;
import com.xiaodou.mapper.AiApplicationMapper;
import com.xiaodou.model.AiAppType;
import com.xiaodou.model.AiApplication;
import com.xiaodou.model.vo.AiAppTypeVO;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.AiAppTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * <p>
 * AI应用类型表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAppTypeServiceImpl extends ServiceImpl<AiAppTypeMapper, AiAppType> implements AiAppTypeService {

    private final AiApplicationMapper workflowMapper;


    @Override
    public IPage<AiAppTypeVO> pageList(Page<AiAppType> page, String name, String tenantId, Byte status) {
        log.info("start page list, name={}, tenantId={}, status={}", name, tenantId, status);

        // 1. 构建动态查询条件
        LambdaQueryWrapper<AiAppType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), AiAppType::getName, name);
        wrapper.eq(StringUtils.hasText(tenantId), AiAppType::getTenantId, tenantId);
        wrapper.eq(status != null, AiAppType::getStatus, status);
        wrapper.orderByDesc(AiAppType::getCreatedAt); // 默认按创建时间降序

        // 2. 执行分页查询
        // ServiceImpl的page方法会查询数据库并填充传入的page对象
        this.page(page, wrapper);

        // 3. 将 Page<AiAppType> 转换为 IPage<AiAppTypeVO>
        // 这里假设 AiAppTypeVO 中有一个能接收 AiAppType 对象的构造函数或静态工厂方法
        // 例如 AiAppTypeVO.fromEntity(aiAppType)
        return page.convert(AiAppTypeVO::fromEntity);
    }

    @Override
    public boolean isDeletable(String typeId) {
        // 查询该类型下是否有工作流
        Long count = workflowMapper.selectCount(new LambdaQueryWrapper<AiApplication>().eq(AiApplication::getTypeId, typeId));
        log.info("isDeletable:{}", count);
        return count == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void safeDelete(String typeId) {
        log.info("start safe delete, typeId:{}", typeId);
        if (!isDeletable(typeId)) {
            throw new AppException(ResultCodeEnum.DATA_ACCESS_ERROR, "该类型下存在关联的工作流，无法删除");
        }

        if (!removeById(typeId)) {
            throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "删除类型失败");
        }
    }
}
