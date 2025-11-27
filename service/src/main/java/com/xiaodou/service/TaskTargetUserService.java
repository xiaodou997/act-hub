package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.TaskTargetUser;
import com.xiaodou.model.dto.task.TaskTargetUserImportDTO;
import com.xiaodou.model.query.TaskTargetUserQuery;

/**
 * <p>
 * 任务定向用户表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
public interface TaskTargetUserService extends IService<TaskTargetUser> {

    /**
     * 导入定向用户
     * @param importDTO 导入数据
     */
    void importTargetUsers(TaskTargetUserImportDTO importDTO);

    /**
     * 分页查询定向用户
     * @param query 查询条件
     * @return 分页数据
     */
    IPage<TaskTargetUser> pageListTargetUsers(TaskTargetUserQuery query);
}