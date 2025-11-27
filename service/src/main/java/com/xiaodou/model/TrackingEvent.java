package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 埋点事件原始记录表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
@Getter
@Setter
@ToString
@TableName("tracking_event")
public class TrackingEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 事件名称 (如 PageView, ButtonClick)
     */
    private String eventName;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 事件发生时间（毫秒级）
     */
    private LocalDateTime createdAt;

    /**
     * 事件扩展属性（JSON）
     */
    private String properties;
}
