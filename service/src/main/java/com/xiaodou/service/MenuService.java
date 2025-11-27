package com.xiaodou.service;

import com.xiaodou.model.Menu;
import com.xiaodou.model.dto.menu.MenuCreateDTO;
import com.xiaodou.model.dto.menu.MenuSortDTO;
import com.xiaodou.model.dto.menu.MenuUpdateDTO;
import com.xiaodou.model.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 根据当前登录用户的权限，构建其可见的菜单树。
     *
     * @return 菜单树列表 (只包含顶级菜单，子菜单在 children 属性中)
     */
    List<MenuVO> buildMenuTreeForCurrentUser();

    /**
     * 获取完整的菜单树（管理端使用，包含所有菜单）
     *
     * @return 完整菜单树列表
     */
    List<MenuVO> getMenuTree();

    /**
     * 根据ID获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单实体
     */
    Menu getById(String id);

    /**
     * 创建菜单
     *
     * @param dto 创建请求DTO
     * @return 创建后的菜单实体
     */
    Menu create(MenuCreateDTO dto);

    /**
     * 更新菜单
     *
     * @param dto 更新请求DTO
     * @return 更新后的菜单实体
     */
    Menu update(MenuUpdateDTO dto);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    void delete(String id);

    /**
     * 检查菜单是否有子菜单
     *
     * @param id 菜单ID
     * @return true表示有子菜单
     */
    boolean hasChildren(String id);

    /**
     * 更新菜单状态
     *
     * @param id     菜单ID
     * @param status 状态（1:正常, 0:禁用）
     */
    void updateStatus(String id, Byte status);

    /**
     * 菜单排序（支持跨父级移动）
     * <p>
     * 将 orderedIds 中的所有菜单移动到 parentId 下，并按顺序重排 sortOrder
     * </p>
     *
     * @param dto 排序请求DTO，包含目标父菜单ID和有序的子菜单ID列表
     */
    void sort(MenuSortDTO dto);
}