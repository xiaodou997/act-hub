package com.xiaodou.model.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色权限分配请求DTO
 */
@Data
@Schema(description = "角色权限分配请求DTO")
public class RolePermissionDTO {

    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色ID不能为空")
    private String roleId;

    @Schema(description = "权限ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "权限ID列表不能为空")
    private List<String> permissionIds;
}
