package com.xiaodou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.ArticleCategory;
import com.xiaodou.model.dto.article.ArticleCategoryCreateDTO;
import com.xiaodou.model.dto.article.ArticleCategoryUpdateDTO;
import com.xiaodou.model.vo.ArticleCategoryVO;

import java.util.List;

/**
 * <p>
 * 教程分类表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
public interface ArticleCategoryService extends IService<ArticleCategory> {

    /**
     * 创建分类
     * @param createDTO 创建DTO
     * @return 分类ID
     */
    String createCategory(ArticleCategoryCreateDTO createDTO);

    /**
     * 更新分类
     * @param updateDTO 更新DTO
     */
    void updateCategory(ArticleCategoryUpdateDTO updateDTO);

    /**
     * 删除分类
     * @param id 分类ID
     */
    void deleteCategory(String id);

    /**
     * 以树形结构获取所有分类
     * @return 分类树
     */
    List<ArticleCategoryVO> listAsTree();
}