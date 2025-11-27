package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户账号认证申请表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString
@TableName("user_account_certification")
public class UserAccountCertification implements Serializable {

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
     * 申请人ID（关联 wechat_user 表）
     */
    private String userId;

    /**
     * 认证平台 (例如: DOUYIN, XIAOHONGSHU)
     */
    private String platform;

    /**
     * 用户在平台上的唯一ID
     */
    private String accountId;

    /**
     * 用户在平台上的昵称
     */
    private String accountName;

    /**
     * 个人主页链接
     */
    private String homepageUrl;

    /**
     * 截图URL列表 (JSON数组)
     */
    private String screenshotUrls;

    /**
     * 审核状态（0:待审核, 1:已通过, 2:已拒绝）
     */
    private Byte status;

    /**
     * 最新一次的审核备注（特别是拒绝理由）
     */
    private String auditNotes;

    /**
     * 最新一次的审核人ID（关联 user 表）
     */
    private String auditorId;

    /**
     * 最新一次的审核时间
     */
    private LocalDateTime auditedAt;

    /**
     * 历史审核记录 (JSON数组)
     */
    private String auditHistory;

    /**
     * 创建时间（首次提交时间）
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 认证状态枚举
     */
    public enum CertificationStatus {
        PENDING((byte) 0, "待审核"),
        APPROVED((byte) 1, "已通过"),
        REJECTED((byte) 2, "已拒绝");

        private final Byte code;
        private final String desc;

        CertificationStatus(Byte code, String desc) {
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
     * 审核历史记录条目
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditHistoryEntry implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        // 提交信息
        private String accountId;
        private String accountName;
        private String homepageUrl;
        private String screenshotUrls;
        private LocalDateTime submittedAt;
        // 审核信息
        private Byte auditStatus;
        private String auditNotes;
        private String auditorId;
        private LocalDateTime auditedAt;
    }
}
