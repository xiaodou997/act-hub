package com.xiaodou.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaodou.model.Article;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "教程文章返回视图对象")
public class ArticleVO {

    @Schema(description = "文章ID")
    private String id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "摘要/简介")
    private String summary;

    @Schema(description = "封面图URL")
    private String coverImageUrl;

    @Schema(description = "内容类型（1:图文, 2:视频）")
    private Byte type;

    @Schema(description = "内容类型描述")
    private String typeDesc;

    @Schema(description = "文章正文")
    private String content;

    @Schema(description = "媒体URL列表")
    private String mediaUrls;

    @Schema(description = "查看量")
    private Integer viewCount;

    @Schema(description = "点赞量")
    private Integer likeCount;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "状态（0:草稿, 1:已发布, 2:已归档）")
    private Byte status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建人ID")
    private String creatorId;

    @Schema(description = "创建人名称")
    private String creatorName; // 关联查询

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "关联的分类列表")
    private List<ArticleCategoryVO> categories;

    public static ArticleVO fromEntity(Article entity) {
        if (entity == null) {
            return null;
        }
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(entity, vo);

        if (entity.getType() != null) {
            for (Article.ArticleType at : Article.ArticleType.values()) {
                if (at.getCode().equals(entity.getType())) {
                    vo.setTypeDesc(at.getDesc());
                    break;
                }
            }
        }

        if (entity.getStatus() != null) {
            for (Article.ArticleStatus as : Article.ArticleStatus.values()) {
                if (as.getCode().equals(entity.getStatus())) {
                    vo.setStatusDesc(as.getDesc());
                    break;
                }
            }
        }
        return vo;
    }
}
