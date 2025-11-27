package com.xiaodou.model.dto.reward;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 奖品实例批量导入请求DTO
 */
@Data
@Schema(description = "奖品实例批量导入请求DTO")
public class RewardItemImportDTO {

    @Schema(description = "关联的奖品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "奖品ID不能为空")
    private String rewardId;

    @Schema(description = "要导入的奖品实例列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "导入列表不能为空")
    @Valid
    private List<RewardItemDetail> items;

    /**
     * 内部类，表示单个奖品实例的详情
     */
    @Data
    @Schema(description = "单个奖品实例详情")
    public static class RewardItemDetail {

        @Schema(description = "实例值（券码或快递单号）", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "实例值不能为空")
        private String itemValue;

        @Schema(description = "预绑定的用户ID（可选）")
        private String targetUserId;

        @Schema(description = "预绑定的手机号（可选）")
        private String targetPhoneNumber;
    }
}
