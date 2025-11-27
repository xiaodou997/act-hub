package com.xiaodou.controller;

import com.xiaodou.model.dto.article.ArticleCategoryCreateDTO;
import com.xiaodou.model.dto.article.ArticleCategoryUpdateDTO;
import com.xiaodou.model.vo.ArticleCategoryVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.ArticleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 教程分类表 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
@Tag(name = "后台-教程分类管理", description = "教程分类的增删改查接口")
@RestController
@RequestMapping("/admin/article-category")
@RequiredArgsConstructor
public class ArticleCategoryController {

    private final ArticleCategoryService articleCategoryService;

    @Operation(summary = "创建教程分类", description = "创建一个新的教程分类")
    @PostMapping
    public Result<String> createCategory(@Valid @RequestBody ArticleCategoryCreateDTO createDTO) {
        String categoryId = articleCategoryService.createCategory(createDTO);
        return Result.success(categoryId);
    }

    @Operation(summary = "更新教程分类", description = "更新一个已有的教程分类")
    @PutMapping
    public Result<Void> updateCategory(@Valid @RequestBody ArticleCategoryUpdateDTO updateDTO) {
        articleCategoryService.updateCategory(updateDTO);
        return Result.success();
    }

    @Operation(summary = "删除教程分类", description = "删除一个教程分类（如果该分类下没有子分类或文章）")
    @Parameter(name = "id", description = "分类ID", required = true)
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable String id) {
        articleCategoryService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "获取分类树", description = "以树形结构获取所有教程分类")
    @GetMapping("/tree")
    public Result<List<ArticleCategoryVO>> getCategoryTree() {
        List<ArticleCategoryVO> tree = articleCategoryService.listAsTree();
        return Result.success(tree);
    }
}
