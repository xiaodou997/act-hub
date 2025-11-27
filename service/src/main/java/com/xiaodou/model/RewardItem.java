package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 奖品实例表（券码/单号库存）
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString
@TableName("reward_item")
public class RewardItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 关联的奖品ID
     */
    private String rewardId;

    /**
     * 实例值（券码或快递单号）
     */
    private String itemValue;

    /**
     * 状态（0:可用, 1:已分配, 2:已作废）
     */
    private Byte status;

    /**
     * 预绑定的用户ID（可选）
     */
    private String targetUserId;

    /**
     * 预绑定的手机号（可选）
     */
    private String targetPhoneNumber;

    /**
     * 导入操作员ID
     */
    private String importerId;

    /**
     * 创建时间（导入时间）
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 奖品实例状态枚举
     */
    public enum RewardItemStatus {
        AVAILABLE((byte) 0, "可用"),
        ASSIGNED((byte) 1, "已分配"),
        VOID((byte) 2, "已作废");

        private final Byte code;
        private final String desc;

        RewardItemStatus(Byte code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Byte getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
