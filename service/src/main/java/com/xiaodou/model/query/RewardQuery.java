package com.xiaodou.model.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 奖品查询对象
 */
@Data
@Schema(description = "奖品查询对象")
public class RewardQuery {

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "奖品名称（模糊查询）")
    private String name;

    @Schema(description = "奖品类型（1:虚拟券码, 2:实物商品）")
    private Byte type;

    @Schema(description = "状态（0:草稿, 1:已上架, 2:已归档）")
    private Byte status;

    @Schema(description = "有效期开始时间（毫秒时间戳）", example = "1735660800000")
    private Long startDate;

    @Schema(description = "有效期结束时间（毫秒时间戳）", example = "1767225599000")
    private Long endDate;
}
