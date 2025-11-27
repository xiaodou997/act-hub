package com.xiaodou.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "教程文章创建请求DTO")
public class ArticleCreateDTO {

    @Schema(description = "文章标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文章标题不能为空")
    private String title;

    @Schema(description = "摘要/简介")
    private String summary;

    @Schema(description = "封面图URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "封面图URL不能为空")
    private String coverImageUrl;

    @Schema(description = "内容类型（1:图文, 2:视频）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "内容类型不能为空")
    private Byte type;

    @Schema(description = "文章正文（HTML或Markdown）")
    private String content;

    @Schema(description = "媒体URL列表（图文时为图片数组，视频时为视频数组）")
    private List<String> mediaUrls;

    @Schema(description = "排序值", defaultValue = "0")
    private Integer sortOrder = 0;

    @Schema(description = "状态（0:草稿, 1:已发布）", defaultValue = "0")
    private Byte status;

    @Schema(description = "关联的分类ID列表")
    private List<String> categoryIds;
}
