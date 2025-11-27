package com.xiaodou.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 奖品配置表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString
public class Reward implements Serializable {

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
     * 奖品名称
     */
    private String name;

    /**
     * 奖品类型（1:虚拟券码, 2:实物商品）
     */
    private Byte type;

    /**
     * 奖品图片URL
     */
    private String imageUrl;

    /**
     * 奖品说明
     */
    private String description;

    /**
     * 使用规则及说明
     */
    private String rules;

    /**
     * 总库存数量
     */
    private Integer totalQuantity;

    /**
     * 已发放数量（缓存计数）
     */
    private Integer issuedQuantity;

    /**
     * 有效期开始时间
     */
    private LocalDateTime startDate;

    /**
     * 有效期结束时间
     */
    private LocalDateTime endDate;

    /**
     * 状态（0:草稿, 1:已上架, 2:已归档）
     */
    private Byte status;

    /**
     * 创建人ID
     */
    private String creatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 奖品类型枚举
     */
    public enum RewardType {
        VIRTUAL((byte) 1, "虚拟券码"),
        PHYSICAL((byte) 2, "实物商品");

        private final Byte code;
        private final String desc;

        RewardType(Byte code, String desc) {
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

    /**
     * 奖品状态枚举
     */
    public enum RewardStatus {
        DRAFT((byte) 0, "草稿"),
        ACTIVE((byte) 1, "已上架"),
        ARCHIVED((byte) 2, "已归档");

        private final Byte code;
        private final String desc;

        RewardStatus(Byte code, String desc) {
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
