package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.Task;
import com.xiaodou.model.dto.task.TaskCreateDTO;
import com.xiaodou.model.dto.task.TaskUpdateDTO;
import com.xiaodou.model.query.TaskQuery;
import com.xiaodou.model.vo.TaskVO;

/**
 * <p>
 * 分发任务表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
public interface TaskService extends IService<Task> {

    /**
     * 创建任务
     * @param createDTO 任务创建数据
     * @param creatorId 创建人ID
     * @return 创建后的任务实体
     */
    Task createTask(TaskCreateDTO createDTO, String creatorId);

    /**
     * 更新任务
     * @param updateDTO 任务更新数据
     * @return 更新后的任务实体
     */
    Task updateTask(TaskUpdateDTO updateDTO);

    /**
     * 安全删除任务（有参与记录则不允许删除）
     * @param taskId 任务ID
     */
    void deleteTask(String taskId);

    /**
     * 分页查询任务列表
     * @param query 查询条件
     * @return 分页的视图对象
     */
    IPage<TaskVO> pageListTasks(TaskQuery query);

    /**
     * 更改任务状态
     * @param taskId 任务ID
     * @param status 目标状态
     */
    void changeTaskStatus(String taskId, Byte status);
}