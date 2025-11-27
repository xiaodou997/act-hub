package com.xiaodou.model.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 奖励发放记录查询对象
 */
@Data
@Schema(description = "奖励发放记录查询对象")
public class RewardPayoutQuery {

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "获奖用户ID")
    private String userId;

    @Schema(description = "关联的任务ID")
    private String taskId;

    @Schema(description = "奖品ID")
    private String rewardId;

    @Schema(description = "发放状态（0:成功, 1:失败）")
    private Byte status;

    @Schema(description = "发放时间范围 - 开始（毫秒时间戳）")
    private Long payoutTimeStart;

    @Schema(description = "发放时间范围 - 结束（毫秒时间戳）")
    private Long payoutTimeEnd;
}
