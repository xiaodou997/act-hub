package com.xiaodou.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 奖品实例查询对象
 */
@Data
@Schema(description = "奖品实例查询对象")
public class RewardItemQuery {

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "关联的奖品ID")
    private String rewardId;

    @Schema(description = "实例值（券码或快递单号，模糊查询）")
    private String itemValue;

    @Schema(description = "状态（0:可用, 1:已分配, 2:已作废）")
    private Byte status;

    @Schema(description = "预绑定的用户ID")
    private String targetUserId;

    @Schema(description = "预绑定的手机号")
    private String targetPhoneNumber;
}
