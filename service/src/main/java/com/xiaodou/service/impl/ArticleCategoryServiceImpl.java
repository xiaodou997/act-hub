package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.exception.AppException;
import com.xiaodou.mapper.ArticleCategoryMapper;
import com.xiaodou.model.ArticleCategory;
import com.xiaodou.model.dto.article.ArticleCategoryCreateDTO;
import com.xiaodou.model.dto.article.ArticleCategoryUpdateDTO;
import com.xiaodou.model.vo.ArticleCategoryVO;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.ArticleCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleCategoryServiceImpl extends ServiceImpl<ArticleCategoryMapper, ArticleCategory> implements ArticleCategoryService {

    @Override
    public String createCategory(ArticleCategoryCreateDTO createDTO) {
        ArticleCategory category = new ArticleCategory();
        BeanUtils.copyProperties(createDTO, category);
        category.setStatus(ArticleCategory.CategoryStatus.ENABLED.getCode()); // 默认启用
        this.save(category);
        return category.getId();
    }

    @Override
    @Transactional
    public void updateCategory(ArticleCategoryUpdateDTO updateDTO) {
        if (updateDTO.getId().equals(updateDTO.getParentId())) {
            throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "不能将分类设置为自己的子分类");
        }
        ArticleCategory category = new ArticleCategory();
        BeanUtils.copyProperties(updateDTO, category);
        this.updateById(category);
    }

    @Override
    @Transactional
    public void deleteCategory(String id) {
        // 检查是否有子分类
        long childrenCount = this.lambdaQuery().eq(ArticleCategory::getParentId, id).count();
        if (childrenCount > 0) {
            throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "该分类下有子分类，无法删除");
        }
        // 检查是否被文章使用 (此处需要 ArticleCategoryMapService，暂时省略)
        // long articleCount = articleCategoryMapService.countByCategoryId(id);
        // if (articleCount > 0) {
        //     throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "该分类已被文章使用，无法删除");
        // }
        this.removeById(id);
    }

    @Override
    public List<ArticleCategoryVO> listAsTree() {
        // 1. 获取所有分类
        List<ArticleCategory> allCategories = this.lambdaQuery()
            .orderByAsc(ArticleCategory::getSortOrder)
            .orderByDesc(ArticleCategory::getCreatedAt)
            .list();

        // 2. 转换为VO对象
        List<ArticleCategoryVO> allCategoryVOs = allCategories.stream()
            .map(ArticleCategoryVO::fromEntity)
            .collect(Collectors.toList());

        // 3. 构建ID到VO的映射
        Map<String, ArticleCategoryVO> map = allCategoryVOs.stream()
            .collect(Collectors.toMap(ArticleCategoryVO::getId, vo -> vo));

        // 4. 构建树形结构
        List<ArticleCategoryVO> tree = new ArrayList<>();
        for (ArticleCategoryVO vo : allCategoryVOs) {
            if ("0".equals(vo.getParentId())) {
                tree.add(vo);
            } else {
                ArticleCategoryVO parent = map.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                }
            }
        }
        return tree;
    }
}