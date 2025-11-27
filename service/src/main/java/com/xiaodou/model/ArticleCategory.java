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
 * 教程分类表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
@Getter
@Setter
@ToString
@TableName("article_category")
public class ArticleCategory implements Serializable {

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
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID，0表示顶级分类
     */
    private String parentId;

    /**
     * 排序值，数字越大越靠前
     */
    private Integer sortOrder;

    /**
     * 状态（0:禁用, 1:启用）
     */
    private Byte status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 分类状态枚举
     */
    public enum CategoryStatus {
        DISABLED((byte) 0, "禁用"),
        ENABLED((byte) 1, "启用");

        private final Byte code;
        private final String desc;

        CategoryStatus(Byte code, String desc) {
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
