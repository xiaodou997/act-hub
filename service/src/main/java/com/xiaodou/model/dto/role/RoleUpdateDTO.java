package com.xiaodou.model.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色更新请求DTO
 */
@Data
@Schema(description = "角色更新请求DTO")
public class RoleUpdateDTO {

    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色ID不能为空")
    private String id;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "平台管理员")
    @NotBlank(message = "角色名称不能为空")
    private String name;

    @Schema(description = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @NotBlank(message = "角色编码不能为空")
    private String code;

    @Schema(description = "描述", example = "拥有所有权限的管理员角色")
    private String description;

    @Schema(description = "是否平台级角色（1-是，0-否）", example = "1")
    private Byte isPlatform;
}
