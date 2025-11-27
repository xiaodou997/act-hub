package com.xiaodou.model.dto.reward;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 奖励发放请求DTO
 */
@Data
@Schema(description = "奖励发放请求DTO")
public class RewardPayoutDTO {

    @Schema(description = "要发放奖励的任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    @Schema(description = "要发放的奖品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "奖品ID不能为空")
    private String rewardId;

    @Schema(description = "发放范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发放范围不能为空")
    private PayoutScope scope;

    /**
     * 发放范围枚举
     */
    public enum PayoutScope {
        /**
         * 所有审核通过的用户
         */
        APPROVED_USERS,
        /**
         * 所有定向的用户
         */
        TARGETED_USERS
    }
}
