package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.exception.AppException;
import com.xiaodou.mapper.ArticleMapper;
import com.xiaodou.model.Article;
import com.xiaodou.model.ArticleCategoryMap;
import com.xiaodou.model.dto.article.ArticleCreateDTO;
import com.xiaodou.model.dto.article.ArticleUpdateDTO;
import com.xiaodou.model.query.ArticleQuery;
import com.xiaodou.model.vo.ArticleVO;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.ArticleCategoryMapService;
import com.xiaodou.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleCategoryMapService articleCategoryMapService;
    private final ObjectMapper objectMapper;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional
    public String createArticle(ArticleCreateDTO createDTO, String creatorId) {
        Article article = new Article();
        BeanUtils.copyProperties(createDTO, article);
        article.setCreatorId(creatorId);

        // 处理媒体URL列表
        if (!CollectionUtils.isEmpty(createDTO.getMediaUrls())) {
            try {
                article.setMediaUrls(objectMapper.writeValueAsString(createDTO.getMediaUrls()));
            } catch (JsonProcessingException e) {
                throw new AppException(ResultCodeEnum.PARAM_FORMAT_ERROR, "媒体URL格式错误");
            }
        }

        // 设置状态和发布时间
        if (Article.ArticleStatus.PUBLISHED.getCode().equals(createDTO.getStatus())) {
            article.setPublishedAt(LocalDateTime.now());
        } else {
            article.setStatus(Article.ArticleStatus.DRAFT.getCode());
        }

        this.save(article);
        String articleId = article.getId();

        // 处理文章与分类的关联
        updateArticleCategoryMapping(articleId, createDTO.getCategoryIds());

        return articleId;
    }

    @Override
    @Transactional
    public void updateArticle(ArticleUpdateDTO updateDTO) {
        Article existingArticle = this.getById(updateDTO.getId());
        if (existingArticle == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "文章不存在");
        }

        Article article = new Article();
        BeanUtils.copyProperties(updateDTO, article);

        // 处理媒体URL列表
        if (updateDTO.getMediaUrls() != null) {
            try {
                article.setMediaUrls(objectMapper.writeValueAsString(updateDTO.getMediaUrls()));
            } catch (JsonProcessingException e) {
                throw new AppException(ResultCodeEnum.PARAM_FORMAT_ERROR, "媒体URL格式错误");
            }
        }

        // 处理发布状态和时间
        if (Article.ArticleStatus.PUBLISHED.getCode().equals(updateDTO.getStatus()) && existingArticle.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }

        this.updateById(article);

        // 如果传入了 categoryIds (即使是空列表)，则更新关联关系
        if (updateDTO.getCategoryIds() != null) {
            updateArticleCategoryMapping(updateDTO.getId(), updateDTO.getCategoryIds());
        }
    }

    /**
     * 统一处理文章与分类的映射关系
     * @param articleId 文章ID
     * @param categoryIds 分类ID列表
     */
    private void updateArticleCategoryMapping(String articleId, List<String> categoryIds) {
        // 1. 删除旧的关联
        articleCategoryMapService.remove(new LambdaQueryWrapper<ArticleCategoryMap>().eq(ArticleCategoryMap::getArticleId, articleId));
        // 2. 如果新的分类列表不为空，则创建新的关联
        if (!CollectionUtils.isEmpty(categoryIds)) {
            List<ArticleCategoryMap> mappings = categoryIds.stream()
                .map(categoryId -> new ArticleCategoryMap(articleId, categoryId))
                .collect(Collectors.toList());
            articleCategoryMapService.saveBatch(mappings);
        }
    }

    @Override
    @Transactional
    public void deleteArticle(String id) {
        // 逻辑删除，更新状态为归档
        Article update = new Article();
        update.setId(id);
        update.setStatus(Article.ArticleStatus.ARCHIVED.getCode());
        this.updateById(update);
    }

    @Override
    public IPage<ArticleVO> pageListArticles(ArticleQuery query) {
        Page<ArticleVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        // 使用自定义的关联查询
        return articleMapper.selectPageWithDetails(page, query);
    }

    @Override
    public ArticleVO getArticleDetail(String id) {
        // 使用自定义的关联查询
        ArticleVO vo = articleMapper.selectDetailById(id);
        if (vo == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "文章不存在");
        }
        return vo;
    }
}