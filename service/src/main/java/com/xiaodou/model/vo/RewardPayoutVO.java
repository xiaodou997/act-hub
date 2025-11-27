package com.xiaodou.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 奖励发放记录返回视图对象
 */
@Data
@Schema(description = "奖励发放记录返回视图对象")
public class RewardPayoutVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "获奖用户ID")
    private String userId;

    @Schema(description = "获奖用户昵称")
    private String userName; // 关联查询

    @Schema(description = "关联的任务ID")
    private String taskId;

    @Schema(description = "任务名称")
    private String taskName; // 关联查询

    @Schema(description = "奖品ID")
    private String rewardId;

    @Schema(description = "奖品名称")
    private String rewardName; // 关联查询

    @Schema(description = "奖品类型（1:虚拟, 2:实物）")
    private Byte rewardType;

    @Schema(description = "奖品类型描述")
    private String rewardTypeDesc;

    @Schema(description = "发放内容（券码或快递单号）")
    private String payoutContent;

    @Schema(description = "发放状态（0:成功, 1:失败）")
    private Byte status;

    @Schema(description = "发放状态描述")
    private String statusDesc;

    @Schema(description = "发放时间（毫秒时间戳）")
    private Long payoutTime;

    @Schema(description = "操作发放的管理员ID")
    private String operatorId;

    @Schema(description = "操作员名称")
    private String operatorName; // 关联查询
}
