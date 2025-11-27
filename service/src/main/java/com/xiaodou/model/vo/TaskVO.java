package com.xiaodou.model.vo;

import com.xiaodou.model.Task;
import com.xiaodou.utils.DateTimeUtils;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务视图对象
 */
@Data
public class TaskVO {

    private String id;

    private String taskName;

    private Long startTime;

    private Long endTime;

    private List<String> platforms;

    private String requirements;

    private List<String> images;

    private BigDecimal rewardAmount;

    private Integer totalQuota;

    private Integer claimedCount;

    private Integer completedCount;

    private Boolean isTargeted;

    private Integer sortOrder;

    private Byte status;

    private String statusDesc; // 任务状态描述

    private String creatorId;

    private Long createdAt;

    private Long updatedAt;

    public static TaskVO fromEntity(Task task) {
        if (task == null) {
            return null;
        }
        TaskVO vo = new TaskVO();
        BeanUtils.copyProperties(task, vo);

        // 转换时间为时间戳
        vo.setStartTime(DateTimeUtils.toTimestampAtUTC8(task.getStartTime()));
        vo.setEndTime(DateTimeUtils.toTimestampAtUTC8(task.getEndTime()));
        vo.setCreatedAt(DateTimeUtils.toTimestampAtUTC8(task.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestampAtUTC8(task.getUpdatedAt()));

        // 处理JSON字符串到List的转换
        // vo.setPlatforms(JSON.parseArray(task.getPlatforms(), String.class)); // 假设使用Fastjson
        // vo.setImages(JSON.parseArray(task.getImages(), String.class)); // 假设使用Fastjson

        // 转换状态描述
        for (Task.TaskStatus ts : Task.TaskStatus.values()) {
            if (ts.getCode().equals(task.getStatus())) {
                vo.setStatusDesc(ts.getDesc());
                break;
            }
        }
        return vo;
    }
}
