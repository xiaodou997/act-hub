package com.xiaodou.model.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 权限更新请求DTO
 */
@Data
@Schema(description = "权限更新请求DTO")
public class PermissionUpdateDTO {

    @Schema(description = "权限ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "权限ID不能为空")
    private String id;

    @Schema(description = "权限名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户列表")
    @NotBlank(message = "权限名称不能为空")
    private String name;

    @Schema(description = "权限编码（唯一标识）", requiredMode = Schema.RequiredMode.REQUIRED, example = "system:user:list")
    @NotBlank(message = "权限编码不能为空")
    private String code;

    @Schema(description = "类型：1-菜单, 2-操作按钮, 3-API", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "权限类型不能为空")
    private Byte type;

    @Schema(description = "描述", example = "查看用户列表的权限")
    private String description;
}
