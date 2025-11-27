package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.mapper.RolePermissionMapper;
import com.xiaodou.model.RolePermission;
import com.xiaodou.service.RolePermissionService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色权限关联表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements
    RolePermissionService {

}
