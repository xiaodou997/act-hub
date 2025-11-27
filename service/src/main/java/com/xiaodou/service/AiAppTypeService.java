package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.AiAppType;
import com.xiaodou.model.vo.AiAppTypeVO;

public interface AiAppTypeService extends IService<AiAppType> {

    IPage<AiAppTypeVO> pageList(Page<AiAppType> page, String name, String tenantId, Byte status);

    boolean isDeletable(String typeId);

    void safeDelete(String typeId);
}
