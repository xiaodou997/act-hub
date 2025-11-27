package com.xiaodou.model.vo;

import com.xiaodou.model.Reward;
import com.xiaodou.utils.DateTimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * 奖品返回视图对象
 */
@Data
@Schema(description = "奖品返回视图对象")
public class RewardVO {

    @Schema(description = "奖品ID")
    private String id;

    @Schema(description = "奖品名称")
    private String name;

    @Schema(description = "奖品类型（1:虚拟券码, 2:实物商品）")
    private Byte type;

    @Schema(description = "奖品类型描述")
    private String typeDesc;

    @Schema(description = "奖品图片URL")
    private String imageUrl;

    @Schema(description = "奖品说明")
    private String description;

    @Schema(description = "使用规则及说明")
    private String rules;

    @Schema(description = "总库存数量")
    private Integer totalQuantity;

    @Schema(description = "已发放数量")
    private Integer issuedQuantity;

    @Schema(description = "有效期开始时间（毫秒时间戳）")
    private Long startDate;

    @Schema(description = "有效期结束时间（毫秒时间戳）")
    private Long endDate;

    @Schema(description = "状态（0:草稿, 1:已上架, 2:已归档）")
    private Byte status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建人ID")
    private String creatorId;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    private Long updatedAt;

    public static RewardVO fromEntity(Reward reward) {
        if (reward == null) {
            return null;
        }
        RewardVO vo = new RewardVO();
        BeanUtils.copyProperties(reward, vo);

        // 转换时间为时间戳
        vo.setStartDate(DateTimeUtils.toTimestampAtUTC8(reward.getStartDate()));
        vo.setEndDate(DateTimeUtils.toTimestampAtUTC8(reward.getEndDate()));
        vo.setCreatedAt(DateTimeUtils.toTimestampAtUTC8(reward.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestampAtUTC8(reward.getUpdatedAt()));

        // 设置类型描述
        if (reward.getType() != null) {
            for (Reward.RewardType rt : Reward.RewardType.values()) {
                if (rt.getCode().equals(reward.getType())) {
                    vo.setTypeDesc(rt.getDesc());
                    break;
                }
            }
        }

        // 设置状态描述
        if (reward.getStatus() != null) {
            for (Reward.RewardStatus rs : Reward.RewardStatus.values()) {
                if (rs.getCode().equals(reward.getStatus())) {
                    vo.setStatusDesc(rs.getDesc());
                    break;
                }
            }
        }
        return vo;
    }
}
