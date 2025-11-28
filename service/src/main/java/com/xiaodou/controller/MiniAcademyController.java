package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaodou.model.vo.ArticleCategoryVO;
import com.xiaodou.model.vo.ArticleVO;
import com.xiaodou.model.ArticleCategory;
import com.xiaodou.model.Article;
import com.xiaodou.result.Result;
import com.xiaodou.service.ArticleCategoryService;
import com.xiaodou.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "小程序-创作学院", description = "分类与文章查询")
@RestController
@RequestMapping("/api/academy")
@RequiredArgsConstructor
public class MiniAcademyController {

    private final ArticleCategoryService categoryService;
    private final ArticleService articleService;

    @GetMapping("/categories")
    @Operation(summary = "分类树")
    public Result<List<ArticleCategoryVO>> categories() {
        return Result.success(categoryService.listAsTree());
    }

    @GetMapping("/articles")
    @Operation(summary = "文章列表")
    public Result<List<ArticleVO>> articles(@RequestParam(required = false) String categoryId) {
        LambdaQueryWrapper<Article> q = new LambdaQueryWrapper<>();
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            q.eq(Article::getCategoryId, categoryId.trim());
        }
        q.eq(Article::getStatus, 1).orderByDesc(Article::getUpdatedAt);
        List<ArticleVO> list = articleService.list(q).stream().map(ArticleVO::fromEntity).collect(Collectors.toList());
        return Result.success(list);
    }
}

