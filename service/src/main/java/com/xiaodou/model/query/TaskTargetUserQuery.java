package com.xiaodou.model.query;

import com.xiaodou.model.vo.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务定向用户查询对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskTargetUserQuery extends PageQuery {

    private String taskId; // 任务ID

    private String userId; // 指定的微信用户ID

    private String phoneNumber; // 指定的手机号
}
