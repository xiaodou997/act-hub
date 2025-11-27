package com.xiaodou.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xiaodou.model.ArticleCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * 教程分类返回视图对象
 */
@Data
@Schema(description = "教程分类返回视图对象")
public class ArticleCategoryVO {

    @Schema(description = "分类ID")
    private String id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父分类ID")
    private String parentId;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "状态（0:禁用, 1:启用）")
    private Byte status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "子分类列表")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ArticleCategoryVO> children;

    public static ArticleCategoryVO fromEntity(ArticleCategory entity) {
        if (entity == null) {
            return null;
        }
        ArticleCategoryVO vo = new ArticleCategoryVO();
        BeanUtils.copyProperties(entity, vo);

        if (entity.getStatus() != null) {
            for (ArticleCategory.CategoryStatus cs : ArticleCategory.CategoryStatus.values()) {
                if (cs.getCode().equals(entity.getStatus())) {
                    vo.setStatusDesc(cs.getDesc());
                    break;
                }
            }
        }
        return vo;
    }
}
