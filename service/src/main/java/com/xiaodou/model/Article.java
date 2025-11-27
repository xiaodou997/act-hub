package com.xiaodou.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 教程文章表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
@Getter
@Setter
@ToString
public class Article implements Serializable {

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
     * 文章标题
     */
    private String title;

    /**
     * 摘要/简介
     */
    private String summary;

    /**
     * 封面图URL
     */
    private String coverImageUrl;

    /**
     * 内容类型（1:图文, 2:视频）
     */
    private Byte type;

    /**
     * 文章正文（HTML或Markdown）
     */
    private String content;

    /**
     * 媒体URL列表（图片数组或视频数组）
     */
    private String mediaUrls;

    /**
     * 查看量
     */
    private Integer viewCount;

    /**
     * 点赞量
     */
    private Integer likeCount;

    /**
     * 排序值，数字越大越靠前
     */
    private Integer sortOrder;

    /**
     * 状态（0:草稿, 1:已发布, 2:已归档）
     */
    private Byte status;

    /**
     * 创建人ID
     */
    private String creatorId;

    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 内容类型枚举
     */
    public enum ArticleType {
        IMAGE_TEXT((byte) 1, "图文"),
        VIDEO((byte) 2, "视频");

        private final Byte code;
        private final String desc;

        ArticleType(Byte code, String desc) {
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
     * 文章状态枚举
     */
    public enum ArticleStatus {
        DRAFT((byte) 0, "草稿"),
        PUBLISHED((byte) 1, "已发布"),
        ARCHIVED((byte) 2, "已归档");

        private final Byte code;
        private final String desc;

        ArticleStatus(Byte code, String desc) {
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
