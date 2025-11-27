package com.xiaodou.model.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 菜单排序请求DTO
 */
@Data
@Schema(description = "菜单排序请求DTO")
public class MenuSortDTO {

    @Schema(description = "目标父菜单ID，null或空字符串表示根层级", example = "parent-id-123")
    private String parentId;

    @Schema(description = "目标父菜单下的子菜单ID有序列表，顺序即为最终显示顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "排序列表不能为空")
    private List<String> orderedIds;
}
