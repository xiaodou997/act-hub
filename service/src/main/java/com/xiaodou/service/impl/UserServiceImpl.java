package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.exception.AppException;
import com.xiaodou.mapper.UserMapper;
import com.xiaodou.mapper.UserRoleMapper;
import com.xiaodou.model.User;
import com.xiaodou.model.UserRole;
import com.xiaodou.model.dto.user.ChangePasswordDTO;
import com.xiaodou.model.dto.user.UserDTO;
import com.xiaodou.model.dto.user.UserPageDTO;
import com.xiaodou.model.dto.user.UserWithRolesDTO;
import com.xiaodou.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * TLC 用户表（平台管理员 + 租户操作员） 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRoleMapper userRoleMapper;

    @Override
    public UserWithRolesDTO selectUserWithRolesByUsername(String username) {
        return baseMapper.selectUserWithRolesByUsername(username);
    }

    @Override
    public Page<UserDTO> selectUsersByPage(Page<UserDTO> page, UserPageDTO req) {
        return baseMapper.selectUsersByPage(page, req);
    }

    @Override
    public void changePassword(String userId, ChangePasswordDTO req) {
        log.info("start change password");
        User user = lambdaQuery().eq(User::getId, userId)
            .one();
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            log.error("old password is invalid");
            throw new AppException(400, "旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        this.updateById(user);
        log.info("change password success");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeOne(String id) {
        baseMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(String userId, List<String> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (!CollectionUtils.isEmpty(roleIds)) {
            ArrayList<UserRole> userRoles = new ArrayList<>();
            for (String roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoles.add(userRole);
            }
            userRoleMapper.insert(userRoles);
        }
    }
}
