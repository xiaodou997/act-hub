package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.Article;
import com.xiaodou.model.dto.article.ArticleCreateDTO;
import com.xiaodou.model.dto.article.ArticleUpdateDTO;
import com.xiaodou.model.query.ArticleQuery;
import com.xiaodou.model.vo.ArticleVO;

/**
 * <p>
 * 教程文章表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
public interface ArticleService extends IService<Article> {

    /**
     * 创建文章
     * @param createDTO 创建DTO
     * @param creatorId 创建人ID
     * @return 文章ID
     */
    String createArticle(ArticleCreateDTO createDTO, String creatorId);

    /**
     * 更新文章
     * @param updateDTO 更新DTO
     */
    void updateArticle(ArticleUpdateDTO updateDTO);

    /**
     * 删除文章（逻辑删除）
     * @param id 文章ID
     */
    void deleteArticle(String id);

    /**
     * 分页查询文章列表
     * @param query 查询条件
     * @return 文章VO分页列表
     */
    IPage<ArticleVO> pageListArticles(ArticleQuery query);

    /**
     * 获取文章详情
     * @param id 文章ID
     * @return 文章VO
     */
    ArticleVO getArticleDetail(String id);
}