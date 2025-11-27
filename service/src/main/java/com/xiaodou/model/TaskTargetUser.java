package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 任务定向用户表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
@TableName("task_target_user")
public class TaskTargetUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 指定的微信用户ID
     */
    private String userId;

    /**
     * 指定的手机号
     */
    private String phoneNumber;
}
