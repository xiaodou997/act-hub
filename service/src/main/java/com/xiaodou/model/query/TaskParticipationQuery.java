package com.xiaodou.model.query;

import com.xiaodou.model.vo.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务参与记录查询对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskParticipationQuery extends PageQuery {

    private String taskId; // 任务ID

    private String userId; // 参与用户ID

    private Byte status; // 参与状态
}
