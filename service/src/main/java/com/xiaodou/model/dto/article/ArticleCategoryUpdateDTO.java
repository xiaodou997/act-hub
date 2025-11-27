package com.xiaodou.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "教程分类更新请求DTO")
public class ArticleCategoryUpdateDTO {

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类ID不能为空")
    private String id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父分类ID")
    private String parentId;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "状态（0:禁用, 1:启用）")
    private Byte status;
}
