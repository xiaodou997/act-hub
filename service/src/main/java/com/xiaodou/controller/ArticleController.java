package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.dto.article.ArticleCreateDTO;
import com.xiaodou.model.dto.article.ArticleUpdateDTO;
import com.xiaodou.model.query.ArticleQuery;
import com.xiaodou.model.vo.ArticleVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 教程文章表 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
@Tag(name = "后台-教程文章管理", description = "教程文章的增删改查接口")
@RestController
@RequestMapping("/admin/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "创建教程文章", description = "创建一个新的教程文章")
    @PostMapping
    public Result<String> createArticle(@Valid @RequestBody ArticleCreateDTO createDTO) {
        String creatorId = UserContextHolder.getUserId();
        String articleId = articleService.createArticle(createDTO, creatorId);
        return Result.success(articleId);
    }

    @Operation(summary = "更新教程文章", description = "更新一个已有的教程文章")
    @PutMapping
    public Result<Void> updateArticle(@Valid @RequestBody ArticleUpdateDTO updateDTO) {
        articleService.updateArticle(updateDTO);
        return Result.success();
    }

    @Operation(summary = "删除教程文章（归档）", description = "逻辑删除文章，将其状态设置为归档")
    @Parameter(name = "id", description = "文章ID", required = true)
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable String id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    @Operation(summary = "分页查询文章列表", description = "根据查询条件分页获取文章列表")
    @GetMapping("/page")
    public Result<IPage<ArticleVO>> pageListArticles(@Valid ArticleQuery query) {
        IPage<ArticleVO> page = articleService.pageListArticles(query);
        return Result.success(page);
    }

    @Operation(summary = "获取文章详情", description = "根据ID获取单个文章的详细信息")
    @Parameter(name = "id", description = "文章ID", required = true)
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable String id) {
        ArticleVO detail = articleService.getArticleDetail(id);
        return Result.success(detail);
    }
}
