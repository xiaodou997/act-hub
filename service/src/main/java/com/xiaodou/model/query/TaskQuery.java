package com.xiaodou.model.query;

import com.xiaodou.model.vo.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务查询对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskQuery extends PageQuery {

    private String taskName; // 任务名称，支持模糊查询

    private Byte status; // 任务状态

    private String platform; // 发布平台，精确匹配

    @Schema(description = "任务开始时间范围-起始（毫秒时间戳）")
    private Long startTimeStart; // 任务开始时间范围-起始

    @Schema(description = "任务开始时间范围-结束（毫秒时间戳）")
    private Long startTimeEnd; // 任务开始时间范围-结束
}
