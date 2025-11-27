package com.xiaodou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.model.AiApplication;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AI 应用配置主表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
public interface AiApplicationService extends IService<AiApplication> {

    Page<AiApplication> pageApplications(Integer current, Integer size, String name, String category, Byte enabled);
}
