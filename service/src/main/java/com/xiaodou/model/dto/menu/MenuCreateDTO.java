package com.xiaodou.model.dto.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单创建请求DTO
 */
@Data
@Schema(description = "菜单创建请求DTO")
@JsonIgnoreProperties(ignoreUnknown = true) // 👈 添加这一行
public class MenuCreateDTO {

    @Schema(description = "父菜单ID，顶级菜单为空或不传", example = "")
    private String parentId;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "系统管理")
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    @Schema(description = "菜单类型（0:目录, 1:菜单, 2:按钮）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "菜单类型不能为空")
    private Byte type;

    @Schema(description = "路由地址（类型为菜单时必填）", example = "/system/user")
    private String path;

    @Schema(description = "组件键名（类型为菜单时必填，前端根据此键名映射组件）", example = "UserManagement")
    private String componentName;

    @Schema(description = "权限标识", example = "system:user:list")
    private String permissionCode;

    @Schema(description = "菜单图标", example = "setting")
    private String icon;

    @Schema(description = "显示排序，数字越小越靠前", defaultValue = "0", example = "1")
    private Integer sortOrder = 0;

    @Schema(description = "菜单状态（1:正常, 0:禁用）", defaultValue = "1", example = "1")
    private Byte status = 1;

    @Schema(description = "是否可见（1:可见, 0:隐藏）", defaultValue = "1", example = "1")
    private Byte isVisible = 1;
}
