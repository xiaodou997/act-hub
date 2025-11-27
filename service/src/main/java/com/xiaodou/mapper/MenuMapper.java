package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaodou.model.Menu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 菜单权限表 Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-16
 */
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 查询所有状态正常且可见的菜单（供超级管理员使用）。
     * <p>
     * 使用 MyBatis-Plus 的 QueryWrapper 直接在接口中实现。
     *
     * @return 菜单列表
     */
    default List<Menu> selectAllVisibleMenus() {
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
               .eq("is_visible", 1)
               .orderByAsc("parent_id", "sort_order");
        return this.selectList(wrapper);
    }

    /**
     * 根据权限标识列表，查询用户可见的菜单。
     * <p>
     * 此方法由 MenuMapper.xml 中的 SQL 实现。
     *
     * @param permissions 用户的权限标识列表
     * @return 菜单列表
     */
    List<Menu> selectMenusByPermissions(@Param("permissions") List<String> permissions);

}
