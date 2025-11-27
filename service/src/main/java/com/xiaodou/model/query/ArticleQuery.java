package com.xiaodou.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "教程文章查询对象")
public class ArticleQuery {

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "文章标题（模糊查询）")
    private String title;

    @Schema(description = "内容类型（1:图文, 2:视频）")
    private Byte type;

    @Schema(description = "状态（0:草稿, 1:已发布, 2:已归档）")
    private Byte status;

    @Schema(description = "分类ID（查询属于该分类的文章）")
    private String categoryId;
}
