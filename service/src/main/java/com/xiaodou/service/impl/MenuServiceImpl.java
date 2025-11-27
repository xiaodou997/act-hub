package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.exception.AppException;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.mapper.MenuMapper;
import com.xiaodou.model.Menu;
import com.xiaodou.model.dto.menu.MenuCreateDTO;
import com.xiaodou.model.dto.menu.MenuSortDTO;
import com.xiaodou.model.dto.menu.MenuUpdateDTO;
import com.xiaodou.model.vo.MenuVO;
import com.xiaodou.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    @Override
    public List<MenuVO> buildMenuTreeForCurrentUser() {
        // 1. 获取当前用户的权限列表
        List<String> permissions = UserContextHolder.getPermissionList();
        boolean isSuperAdmin = UserContextHolder.isSuperAdmin();

        // 2. 从数据库查询该用户可见的所有菜单项
        List<Menu> accessibleMenus;
        if (isSuperAdmin) {
            // 超级管理员获取所有状态正常的菜单
            accessibleMenus = menuMapper.selectAllVisibleMenus();
        } else {
            // 普通用户根据权限列表获取菜单
            if (permissions == null || permissions.isEmpty()) {
                return new ArrayList<>(); // 如果没有任何权限，返回空菜单
            }
            accessibleMenus = menuMapper.selectMenusByPermissions(permissions);
        }

        // 3. 将扁平的菜单列表构建成树形结构
        return buildTree(accessibleMenus);
    }

    /**
     * 将扁平的菜单列表构建成树形结构。
     *
     * @param menus 扁平的菜单列表
     * @return 树形结构的菜单列表
     */
    private List<MenuVO> buildTree(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用 Map 存储所有节点，以ID为键，方便快速查找
        Map<String, MenuVO> nodeMap = menus.stream()
            .map(MenuVO::fromEntity)
            .collect(Collectors.toMap(MenuVO::getId, node -> node));

        List<MenuVO> tree = new ArrayList<>();

        for (MenuVO node : nodeMap.values()) {
            String parentId = node.getParentId();
            if (parentId == null || parentId.isEmpty()) {
                // 如果是顶级节点，直接加入到树中
                tree.add(node);
            } else {
                // 如果是子节点，找到它的父节点并把自己加进去
                MenuVO parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    parentNode.getChildren()
                        .add(node);
                }
            }
        }

        // 对树进行排序
        sortTree(tree);

        return tree;
    }

    /**
     * 递归排序菜单树
     */
    private void sortTree(List<MenuVO> tree) {
        if (tree == null || tree.isEmpty()) {
            return;
        }
        tree.sort(Comparator.comparing(MenuVO::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())));
        for (MenuVO node : tree) {
            sortTree(node.getChildren());
        }
    }

    @Override
    public List<MenuVO> getMenuTree() {
        // 查询所有菜单（包括禁用和隐藏的，管理端需要看到全部）
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Menu::getParentId, Menu::getSortOrder);
        List<Menu> allMenus = menuMapper.selectList(wrapper);

        return buildTree(allMenus);
    }

    @Override
    public Menu getById(String id) {
        return menuMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Menu create(MenuCreateDTO dto) {
        log.info("创建菜单: {}", dto);

        // 校验父菜单是否存在
        if (StringUtils.hasText(dto.getParentId())) {
            Menu parentMenu = menuMapper.selectById(dto.getParentId());
            if (parentMenu == null) {
                throw new AppException(ResultCodeEnum.BAD_REQUEST, "父菜单不存在");
            }
        }

        // 校验权限标识是否重复
        if (StringUtils.hasText(dto.getPermissionCode())) {
            LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Menu::getPermissionCode, dto.getPermissionCode());
            Long count = menuMapper.selectCount(wrapper);
            if (count > 0) {
                throw new AppException(ResultCodeEnum.BAD_REQUEST, "权限标识已存在");
            }
        }

        Menu menu = new Menu();
        menu.setParentId(dto.getParentId());
        menu.setName(dto.getName());
        menu.setType(dto.getType());
        menu.setPath(dto.getPath());
        menu.setComponentName(dto.getComponentName());
        menu.setPermissionCode(dto.getPermissionCode());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(dto.getSortOrder());
        menu.setStatus(dto.getStatus());
        menu.setIsVisible(dto.getIsVisible());

        menuMapper.insert(menu);
        log.info("菜单创建成功, id: {}", menu.getId());
        return menu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Menu update(MenuUpdateDTO dto) {
        log.info("更新菜单: {}", dto);

        Menu menu = menuMapper.selectById(dto.getId());
        if (menu == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "菜单不存在");
        }

        // 不能将自己设为自己的父菜单
        if (dto.getId().equals(dto.getParentId())) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "不能将自己设为父菜单");
        }

        // 校验父菜单是否存在
        if (StringUtils.hasText(dto.getParentId())) {
            Menu parentMenu = menuMapper.selectById(dto.getParentId());
            if (parentMenu == null) {
                throw new AppException(ResultCodeEnum.BAD_REQUEST, "父菜单不存在");
            }
            // 检查是否形成循环（父菜单不能是自己的子菜单）
            if (isChildMenu(dto.getId(), dto.getParentId())) {
                throw new AppException(ResultCodeEnum.BAD_REQUEST, "不能选择子菜单作为父菜单");
            }
        }

        // 校验权限标识是否重复（排除自己）
        if (StringUtils.hasText(dto.getPermissionCode())) {
            LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Menu::getPermissionCode, dto.getPermissionCode())
                   .ne(Menu::getId, dto.getId());
            Long count = menuMapper.selectCount(wrapper);
            if (count > 0) {
                throw new AppException(ResultCodeEnum.BAD_REQUEST, "权限标识已存在");
            }
        }

        menu.setParentId(dto.getParentId());
        menu.setName(dto.getName());
        menu.setType(dto.getType());
        menu.setPath(dto.getPath());
        menu.setComponentName(dto.getComponentName());
        menu.setPermissionCode(dto.getPermissionCode());
        menu.setIcon(dto.getIcon());
        if (dto.getSortOrder() != null) {
            menu.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            menu.setStatus(dto.getStatus());
        }
        if (dto.getIsVisible() != null) {
            menu.setIsVisible(dto.getIsVisible());
        }

        menuMapper.updateById(menu);
        log.info("菜单更新成功, id: {}", menu.getId());
        return menu;
    }

    /**
     * 检查targetId是否是menuId的子菜单
     */
    private boolean isChildMenu(String menuId, String targetId) {
        Menu target = menuMapper.selectById(targetId);
        while (target != null && StringUtils.hasText(target.getParentId())) {
            if (target.getParentId().equals(menuId)) {
                return true;
            }
            target = menuMapper.selectById(target.getParentId());
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        log.info("删除菜单, id: {}", id);

        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "菜单不存在");
        }

        // 检查是否有子菜单
        if (hasChildren(id)) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "存在子菜单，无法删除");
        }

        menuMapper.deleteById(id);
        log.info("菜单删除成功, id: {}", id);
    }

    @Override
    public boolean hasChildren(String id) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId, id);
        return menuMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String id, Byte status) {
        log.info("更新菜单状态, id: {}, status: {}", id, status);

        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "菜单不存在");
        }

        menu.setStatus(status);
        menuMapper.updateById(menu);
        log.info("菜单状态更新成功, id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(MenuSortDTO dto) {
        String targetParentId = dto.getParentId();
        List<String> orderedIds = dto.getOrderedIds();

        log.info("菜单排序, parentId: {}, orderedIds: {}", targetParentId, orderedIds);

        // 1. 校验 orderedIds 不能有重复
        Set<String> idSet = new HashSet<>(orderedIds);
        if (idSet.size() != orderedIds.size()) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "排序列表中存在重复的菜单ID");
        }

        // 2. 校验目标父菜单（如果非空）
        // 空字符串或 "0" 视为根层级
        boolean isRootLevel = !StringUtils.hasText(targetParentId) || "0".equals(targetParentId);
        if (!isRootLevel) {
            Menu parentMenu = menuMapper.selectById(targetParentId);
            if (parentMenu == null) {
                throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "目标父菜单不存在");
            }
            // 按钮类型不能作为父级
            if (parentMenu.getType() != null && parentMenu.getType() == 2) {
                throw new AppException(ResultCodeEnum.BAD_REQUEST, "按钮类型不能作为父菜单");
            }
        }

        // 3. 查询所有待排序的菜单，校验是否存在
        List<Menu> menusToSort = menuMapper.selectBatchIds(orderedIds);
        if (menusToSort.size() != orderedIds.size()) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "部分菜单不存在");
        }

        // 转为 Map 便于查找
        Map<String, Menu> menuMap = menusToSort.stream()
            .collect(Collectors.toMap(Menu::getId, m -> m));

        // 4. 检查是否会形成循环（不能把某节点移动到其自身或其后代为父）
        String finalParentId = isRootLevel ? null : targetParentId;
        for (String menuId : orderedIds) {
            // 不能将自己设为自己的父级
            if (menuId.equals(finalParentId)) {
                throw new AppException(ResultCodeEnum.BAD_REQUEST, "不能将菜单移动到自身下");
            }
            // 不能将菜单移动到其子孙节点下
            if (finalParentId != null && isDescendant(menuId, finalParentId)) {
                throw new AppException(ResultCodeEnum.BAD_REQUEST, "不能将菜单移动到其子菜单下，会形成循环");
            }
        }

        // 5. 收集所有被移动菜单的原父级（用于后续归一化）
        Set<String> affectedParentIds = new HashSet<>();
        for (Menu menu : menusToSort) {
            String oldParentId = menu.getParentId();
            // 如果原父级与目标父级不同，记录原父级
            if (!isSameParent(oldParentId, finalParentId)) {
                if (StringUtils.hasText(oldParentId)) {
                    affectedParentIds.add(oldParentId);
                }
            }
        }

        // 6. 更新所有菜单的 parentId 和 sortOrder（按数组索引）
        for (int i = 0; i < orderedIds.size(); i++) {
            String menuId = orderedIds.get(i);
            Menu menu = menuMap.get(menuId);
            menu.setParentId(finalParentId);
            menu.setSortOrder(i);  // 使用数组索引作为排序值
            menuMapper.updateById(menu);
        }

        // 7. 归一化原父级下剩余子节点的 sortOrder（避免出现间隙）
        for (String oldParentId : affectedParentIds) {
            normalizeChildrenSortOrder(oldParentId);
        }

        log.info("菜单排序完成, parentId: {}, count: {}", targetParentId, orderedIds.size());
    }

    /**
     * 检查 descendantId 是否是 ancestorId 的后代
     */
    private boolean isDescendant(String ancestorId, String descendantId) {
        Menu current = menuMapper.selectById(descendantId);
        while (current != null && StringUtils.hasText(current.getParentId())) {
            if (current.getParentId().equals(ancestorId)) {
                return true;
            }
            current = menuMapper.selectById(current.getParentId());
        }
        return false;
    }

    /**
     * 判断两个 parentId 是否相同（都为空/null 也视为相同）
     */
    private boolean isSameParent(String parentId1, String parentId2) {
        boolean isEmpty1 = !StringUtils.hasText(parentId1);
        boolean isEmpty2 = !StringUtils.hasText(parentId2);
        if (isEmpty1 && isEmpty2) {
            return true;
        }
        if (isEmpty1 || isEmpty2) {
            return false;
        }
        return parentId1.equals(parentId2);
    }

    /**
     * 归一化指定父节点下所有子节点的 sortOrder
     */
    private void normalizeChildrenSortOrder(String parentId) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId, parentId)
               .orderByAsc(Menu::getSortOrder);
        List<Menu> children = menuMapper.selectList(wrapper);

        for (int i = 0; i < children.size(); i++) {
            Menu child = children.get(i);
            if (child.getSortOrder() == null || child.getSortOrder() != i) {
                child.setSortOrder(i);
                menuMapper.updateById(child);
            }
        }
    }
}