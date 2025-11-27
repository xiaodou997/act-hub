package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 教程与分类关联表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TableName("article_category_map")
public class ArticleCategoryMap implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 分类ID
     */
    private String categoryId;

    public ArticleCategoryMap(String articleId, String categoryId) {
        this.articleId = articleId;
        this.categoryId = categoryId;
    }
}
