package com.xiaodou.model.dto.reward;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 奖品创建请求DTO
 */
@Data
@Schema(description = "奖品创建请求DTO")
public class RewardCreateDTO {

    @Schema(description = "奖品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "奖品名称不能为空")
    private String name;

    @Schema(description = "奖品类型（1:虚拟券码, 2:实物商品）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "奖品类型不能为空")
    @Min(value = 1, message = "奖品类型不合法")
    private Byte type;

    @Schema(description = "奖品图片URL")
    private String imageUrl;

    @Schema(description = "奖品说明")
    private String description;

    @Schema(description = "使用规则及说明")
    private String rules;

    @Schema(description = "总库存数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "总库存数量不能为空")
    @Min(value = 0, message = "总库存数量不能小于0")
    private Integer totalQuantity;

    @Schema(description = "有效期开始时间", example = "2025-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @Schema(description = "有效期结束时间", example = "2025-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    @Schema(description = "状态（0:草稿, 1:已上架, 2:已归档）", defaultValue = "0")
    private Byte status;
}
