package com.xiaodou.model.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务创建 DTO
 */
@Data
public class TaskCreateDTO {

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @NotNull(message = "任务开始时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "任务结束时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotNull(message = "发布平台不能为空")
    private List<String> platforms; // 存储平台列表，例如 ["XIAOHONGSHU", "DOUYIN"]

    private String requirements; // 补充要求及规则

    private List<String> images; // 任务相关图片URL列表

    @NotNull(message = "任务奖励金额不能为空")
    private BigDecimal rewardAmount;

    @NotNull(message = "任务总量不能为空")
    private Integer totalQuota;

    private Boolean isTargeted = false; // 是否为定向任务

    private Integer sortOrder = 0; // 任务权重

    private Byte status = 0; // 任务状态，默认为草稿
}
