package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.mapper.TaskTargetUserMapper;
import com.xiaodou.model.TaskTargetUser;
import com.xiaodou.model.dto.task.TaskTargetUserImportDTO;
import com.xiaodou.model.query.TaskTargetUserQuery;
import com.xiaodou.service.TaskTargetUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 任务定向用户表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskTargetUserServiceImpl extends ServiceImpl<TaskTargetUserMapper, TaskTargetUser> implements TaskTargetUserService {

    @Override
    @Transactional
    public void importTargetUsers(TaskTargetUserImportDTO importDTO) {
        String taskId = importDTO.getTaskId();
        List<String> identifiers = importDTO.getUserIdentifiers();

        // 导入前先清空该任务之前的所有定向用户
        this.remove(new LambdaQueryWrapper<TaskTargetUser>().eq(TaskTargetUser::getTaskId, taskId));

        List<TaskTargetUser> targetUsers = identifiers.stream().map(identifier -> {
            TaskTargetUser targetUser = new TaskTargetUser();
            targetUser.setTaskId(taskId);
            // 明确：所有传入的标识符都视为用户ID
            targetUser.setUserId(identifier);
            return targetUser;
        }).collect(Collectors.toList());

        this.saveBatch(targetUsers);
    }

    @Override
    public IPage<TaskTargetUser> pageListTargetUsers(TaskTargetUserQuery query) {
        Page<TaskTargetUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<TaskTargetUser> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(query.getTaskId() != null, TaskTargetUser::getTaskId, query.getTaskId());
        wrapper.eq(query.getUserId() != null, TaskTargetUser::getUserId, query.getUserId());
        wrapper.like(StringUtils.hasText(query.getPhoneNumber()), TaskTargetUser::getPhoneNumber, query.getPhoneNumber());

        return this.page(page, wrapper);
    }
}