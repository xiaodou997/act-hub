package com.xiaodou.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xiaodou.model.Menu;
import com.xiaodou.utils.DateTimeUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单视图对象 (View Object)
 * <p>
 * 用于向前端返回层级清晰的菜单树结构，时间字段转换为毫秒时间戳。
 * </p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY) // 如果 children 为空列表，则不序列化，保持JSON清爽
public class MenuVO {

    /**
     * 菜单ID
     */
    private String id;

    /**
     * 父菜单ID
     */
    private String parentId;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单类型（0:目录, 1:菜单, 2:按钮）
     */
    private Byte type;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件键名（前端根据此键名映射到实际组件）
     */
    private String componentName;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 菜单状态（1:正常, 0:禁用）
     */
    private Byte status;

    /**
     * 是否可见（1:可见, 0:隐藏，隐藏后不会在侧边栏显示）
     */
    private Byte isVisible;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;

    /**
     * 子菜单列表
     */
    private List<MenuVO> children = new ArrayList<>();

    /**
     * 从实体对象转换
     */
    public static MenuVO fromEntity(Menu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setName(menu.getName());
        vo.setType(menu.getType());
        vo.setPath(menu.getPath());
        vo.setComponentName(menu.getComponentName());
        vo.setIcon(menu.getIcon());
        vo.setSortOrder(menu.getSortOrder());
        vo.setStatus(menu.getStatus());
        vo.setIsVisible(menu.getIsVisible());
        vo.setCreatedAt(DateTimeUtils.toTimestamp(menu.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestamp(menu.getUpdatedAt()));
        return vo;
    }
}
