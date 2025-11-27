package com.xiaodou.model.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 任务参与审核 DTO
 */
@Data
public class TaskParticipationAuditDTO {

    @NotNull(message = "审核结果不能为空")
    private Byte auditStatus; // 审核结果：3-通过, 4-拒绝

    @NotBlank(message = "审核备注不能为空", groups = {RejectValidation.class})
    private String auditNotes; // 审核备注（拒绝时必填）

    // 定义一个用于拒绝时校验的接口
    public interface RejectValidation {}
}
