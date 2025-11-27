package com.xiaodou.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "教程文章更新请求DTO")
public class ArticleUpdateDTO {

    @Schema(description = "文章ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文章ID不能为空")
    private String id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "摘要/简介")
    private String summary;

    @Schema(description = "封面图URL")
    private String coverImageUrl;

    @Schema(description = "内容类型（1:图文, 2:视频）")
    private Byte type;

    @Schema(description = "文章正文（HTML或Markdown）")
    private String content;

    @Schema(description = "媒体URL列表（图文时为图片数组，视频时为视频数组）")
    private List<String> mediaUrls;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "状态（0:草稿, 1:已发布, 2:已归档）")
    private Byte status;

    @Schema(description = "关联的分类ID列表 (如果此字段不为null，则会覆盖原有关联)")
    private List<String> categoryIds;
}
