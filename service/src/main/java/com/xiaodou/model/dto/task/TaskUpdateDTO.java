package com.xiaodou.model.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务更新 DTO
 */
@Data
public class TaskUpdateDTO {

    @NotNull(message = "任务ID不能为空")
    private String id;

    private String taskName;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private List<String> platforms;

    private String requirements;

    private List<String> images;

    private BigDecimal rewardAmount;

    private Integer totalQuota;

    private Boolean isTargeted;

    private Integer sortOrder;

    private Byte status;
}
