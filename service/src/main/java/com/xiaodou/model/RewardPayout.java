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
 * 奖励发放记录表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString
@TableName("reward_payout")
public class RewardPayout implements Serializable {

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
     * 获奖用户ID
     */
    private String userId;

    /**
     * 关联的任务ID
     */
    private String taskId;

    /**
     * 任务参与记录ID
     */
    private String taskParticipationId;

    /**
     * 奖品ID（快照）
     */
    private String rewardId;

    /**
     * 奖品实例ID
     */
    private String rewardItemId;

    /**
     * 奖品类型（快照, 1:虚拟, 2:实物）
     */
    private Byte rewardType;

    /**
     * 发放内容（快照, 券码或快递单号）
     */
    private String payoutContent;

    /**
     * 发放状态（0:成功, 1:失败）
     */
    private Byte status;

    /**
     * 发放时间
     */
    private LocalDateTime payoutTime;

    /**
     * 操作发放的管理员ID
     */
    private String operatorId;

    /**
     * 发放状态枚举
     */
    public enum PayoutStatus {
        SUCCESS((byte) 0, "成功"),
        FAILED((byte) 1, "失败");

        private final Byte code;
        private final String desc;

        PayoutStatus(Byte code, String desc) {
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
