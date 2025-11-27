package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.model.Article;
import com.xiaodou.model.query.ArticleQuery;
import com.xiaodou.model.vo.ArticleVO;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 教程文章表 Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 自定义分页查询，关联用户表和分类表
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页的视图对象
     */
    IPage<ArticleVO> selectPageWithDetails(IPage<ArticleVO> page, @Param("query") ArticleQuery query);

    /**
     * 根据ID查询单个详情，关联用户表和分类表
     * @param id 文章ID
     * @return 视图对象
     */
    ArticleVO selectDetailById(@Param("id") String id);
}
