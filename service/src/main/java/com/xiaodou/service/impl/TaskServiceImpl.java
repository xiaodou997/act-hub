package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.exception.AppException;
import com.xiaodou.mapper.TaskMapper;
import com.xiaodou.mapper.TaskParticipationMapper;
import com.xiaodou.model.Task;
import com.xiaodou.model.TaskParticipation;
import com.xiaodou.model.dto.task.TaskCreateDTO;
import com.xiaodou.model.dto.task.TaskUpdateDTO;
import com.xiaodou.model.query.TaskQuery;
import com.xiaodou.model.vo.TaskVO;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.TaskService;
import com.xiaodou.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 分发任务表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    private final ObjectMapper objectMapper; // 用于JSON转换
    private final TaskParticipationMapper taskParticipationMapper;

    @Override
    public Task createTask(TaskCreateDTO createDTO, String creatorId) {
        Task task = new Task();
        BeanUtils.copyProperties(createDTO, task);

        try {
            // 将List转换为JSON字符串
            task.setPlatforms(objectMapper.writeValueAsString(createDTO.getPlatforms()));
            task.setImages(objectMapper.writeValueAsString(createDTO.getImages()));
        } catch (JsonProcessingException e) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "平台或图片数据格式错误");
        }

        task.setCreatorId(creatorId);
        task.setClaimedCount(0);
        task.setCompletedCount(0);

        this.save(task);
        return task;
    }

    @Override
    public Task updateTask(TaskUpdateDTO updateDTO) {
        Task existingTask = this.getById(updateDTO.getId());
        if (existingTask == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "任务不存在");
        }

        BeanUtils.copyProperties(updateDTO, existingTask, "id", "creatorId", "createdAt");

        try {
            if (updateDTO.getPlatforms() != null) {
                existingTask.setPlatforms(objectMapper.writeValueAsString(updateDTO.getPlatforms()));
            }
            if (updateDTO.getImages() != null) {
                existingTask.setImages(objectMapper.writeValueAsString(updateDTO.getImages()));
            }
        } catch (JsonProcessingException e) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "平台或图片数据格式错误");
        }

        this.updateById(existingTask);
        return existingTask;
    }

    @Override
    public void deleteTask(String taskId) {
        // 检查是否有用户参与
        Long participationCount = taskParticipationMapper.selectCount(
            new LambdaQueryWrapper<TaskParticipation>().eq(TaskParticipation::getTaskId, taskId)
        );
        if (participationCount > 0) {
            throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "该任务已有用户参与，无法删除");
        }
        this.removeById(taskId);
    }

    @Override
    public IPage<TaskVO> pageListTasks(TaskQuery query) {
        Page<Task> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getTaskName()), Task::getTaskName, query.getTaskName());
        wrapper.eq(query.getStatus() != null, Task::getStatus, query.getStatus());
        if (StringUtils.hasText(query.getPlatform())) {
            // 模糊匹配JSON字符串
            wrapper.like(Task::getPlatforms, "\"" + query.getPlatform() + "\"");
        }
        wrapper.ge(query.getStartTimeStart() != null, Task::getStartTime, DateTimeUtils.toLocalDateTimeAtUTC8(query.getStartTimeStart()));
        wrapper.le(query.getStartTimeEnd() != null, Task::getStartTime, DateTimeUtils.toLocalDateTimeAtUTC8(query.getStartTimeEnd()));

        wrapper.orderByDesc(Task::getSortOrder).orderByDesc(Task::getCreatedAt);

        this.page(page, wrapper);
        return page.convert(TaskVO::fromEntity);
    }

    @Override
    public void changeTaskStatus(String taskId, Byte status) {
        Task task = this.getById(taskId);
        if (task == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "任务不存在");
        }
        // 可在此处增加状态流转的校验逻辑
        Task updateTask = new Task();
        updateTask.setId(taskId);
        updateTask.setStatus(status);
        this.updateById(updateTask);
    }
}