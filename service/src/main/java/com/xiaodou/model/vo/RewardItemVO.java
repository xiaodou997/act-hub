package com.xiaodou.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaodou.model.RewardItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * 奖品实例返回视图对象
 */
@Data
@Schema(description = "奖品实例返回视图对象")
public class RewardItemVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "关联的奖品ID")
    private String rewardId;

    @Schema(description = "奖品名称")
    private String rewardName; // 关联查询

    @Schema(description = "实例值（券码或快递单号）")
    private String itemValue;

    @Schema(description = "状态（0:可用, 1:已分配, 2:已作废）")
    private Byte status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "预绑定的用户ID")
    private String targetUserId;

    @Schema(description = "预绑定用户的昵称")
    private String targetUserName; // 关联查询

    @Schema(description = "预绑定的手机号")
    private String targetPhoneNumber;

    @Schema(description = "导入操作员ID")
    private String importerId;

    @Schema(description = "导入操作员名称")
    private String importerName; // 关联查询

    @Schema(description = "创建时间（导入时间）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static RewardItemVO fromEntity(RewardItem item) {
        if (item == null) {
            return null;
        }
        RewardItemVO vo = new RewardItemVO();
        BeanUtils.copyProperties(item, vo);

        // 设置状态描述
        if (item.getStatus() != null) {
            for (RewardItem.RewardItemStatus ris : RewardItem.RewardItemStatus.values()) {
                if (ris.getCode().equals(item.getStatus())) {
                    vo.setStatusDesc(ris.getDesc());
                    break;
                }
            }
        }
        return vo;
    }
}
