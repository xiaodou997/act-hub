package com.xiaodou.model.dto.task;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 任务定向用户导入 DTO
 */
@Data
public class TaskTargetUserImportDTO {

    @NotNull(message = "任务ID不能为空")
    private String taskId;

    @NotNull(message = "导入的用户标识列表不能为空")
    @Size(min = 1, message = "导入的用户标识列表不能为空")
    private List<String> userIdentifiers; // 可以是 userId 或 phoneNumber
}
