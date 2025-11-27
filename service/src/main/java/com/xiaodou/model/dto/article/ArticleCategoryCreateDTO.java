package com.xiaodou.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "教程分类创建请求DTO")
public class ArticleCategoryCreateDTO {

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "父分类ID，顶级分类为'0'", defaultValue = "0")
    private String parentId = "0";

    @Schema(description = "排序值，数字越大越靠前", defaultValue = "0")
    private Integer sortOrder = 0;
}
