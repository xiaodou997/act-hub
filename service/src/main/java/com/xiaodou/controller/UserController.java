package com.xiaodou.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.model.LoginUser;
import com.xiaodou.model.User;
import com.xiaodou.model.UserRole;
import com.xiaodou.model.dto.user.ChangePasswordDTO;
import com.xiaodou.model.dto.user.UserDTO;
import com.xiaodou.model.dto.user.UserPageDTO;
import com.xiaodou.model.dto.user.UserUpdateDTO;
import com.xiaodou.model.vo.UserVO;
import com.xiaodou.result.Result;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.UserRoleService;
import com.xiaodou.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * TLC 用户表（平台管理员 + 租户操作员） 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleService userRoleService;

    /**
     * 创建用户
     *
     * @param createDTO 用户创建信息
     * @return 用户创建结果，包含创建的用户信息 {@link UserVO}
     */
    @PreAuthorize("hasAnyRole('SYS_ADMIN','SUPER_ADMIN')")
    @PostMapping
    @SystemLog(module = "用户管理", action = "创建用户", description = "创建用户", recordResponse = true)
    public Result<UserVO> create(@RequestBody User createDTO) {
        log.info("create user: {}", createDTO);
        String userId = UserContextHolder.getUserId();
        String tenantId = "1";

        if (StringUtils.isBlank(userId)) {
            log.warn("Create tenant failed, userId is empty");
            return Result.fail("登录用户ID不存在");
        }
        String email = createDTO.getEmail();
        if (StringUtils.isBlank(email)) {
            log.warn("Create user failed, email is empty");
            return Result.fail(ResultCodeEnum.BAD_REQUEST.getCode(), "用户邮箱不能为空");
        }

        User one = userService.lambdaQuery()
            .eq(User::getUsername, email)
            .one();
        if (one != null) {
            log.warn("Create user failed, username already exist");
            return Result.fail(ResultCodeEnum.DATA_EXISTS.getCode(), "邮箱已存在");
        }

        User user = new User();
        user.setTenantId(tenantId);
        user.setUsername(email);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setEmail(email);
        user.setStatus((byte)1);
        userService.save(user);
        log.info("Created user successfully: {}", user);
        // 用户角色
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId("2");
        userRoleService.save(userRole);
        return Result.success(UserVO.fromEntity(user));
    }

    /**
     * 修改密码
     *
     * @param req 密码修改请求参数 {@link ChangePasswordDTO}
     * @return 操作结果，成功返回空字符串，失败返回错误信息
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码")
    @SystemLog(module = "用户管理", action = "修改密码", description = "修改密码")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordDTO req) {
        try {
            String userId = UserContextHolder.getUserId();
            log.info("change password,userId: {}", userId);
            userService.changePassword(userId, req);
            return Result.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     *
     * @param dto 用户更新信息 {@link UserUpdateDTO}
     * @return 操作结果，成功返回空字符串，失败返回错误信息
     */
    @PreAuthorize("hasAnyRole('SYS_ADMIN','SUPER_ADMIN')")
    @PutMapping
    @Operation(summary = "更新用户信息")
    @SystemLog(module = "用户管理", action = "更新用户", description = "更新用户信息")
    public Result<String> update(@RequestBody @Validated UserUpdateDTO dto) {
        log.info("start update user : {}", dto);
        try {
            User dbUser = userService.getById(dto.getId());
            if (dbUser == null) {
                log.error("update , user not exist");
                return Result.fail("用户不存在");
            }

            if (Objects.equals(dbUser.getUsername(), "admin")) {
                log.error("do not update for user admin");
                return Result.fail(ResultCodeEnum.FORBIDDEN);
            }

            User user = new User();
            user.setId(dto.getId());
            user.setTenantId(dto.getTenantId());
            // user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setStatus(dto.getStatus());
            user.setRemark(dto.getRemark());
            userService.updateById(user);
            return Result.success();
        } catch (Exception e) {
            log.error("update user error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @return 操作结果，成功返回空字符串，失败返回错误信息
     */
    @PreAuthorize("hasAnyRole('SYS_ADMIN','SUPER_ADMIN')")
    @PutMapping("/reset-password/{id}")
    @Operation(summary = "重置用户密码")
    @SystemLog(module = "用户管理", action = "重置密码")
    public Result<String> resetPassword(@PathVariable String id) {
        log.info("start reset password for user id: {}", id);
        try {
            User dbUser = userService.getById(id);
            if (dbUser == null) {
                log.error("reset password, user not exist");
                return Result.fail("用户不存在");
            }
            if (Objects.equals(dbUser.getUsername(), "admin")) {
                log.error("do not reset password for user admin");
                return Result.fail(ResultCodeEnum.FORBIDDEN);
            }

            User user = new User();
            user.setId(id);
            user.setPassword(passwordEncoder.encode("123456")); // 默认密码123456
            userService.updateById(user);
            log.info("reset password success");
            return Result.success();
        } catch (Exception e) {
            log.error("reset password error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 分页查询成员信息
     *
     * @param req 分页查询请求参数 {@link UserPageDTO}
     * @return 分页查询结果，包含用户信息列表 {@link Page<UserVO>}
     */
    @PreAuthorize("hasAnyRole('SYS_ADMIN','SUPER_ADMIN')")
    @GetMapping("/page")
    @Operation(summary = "分页查询成员信息")
    public Result<Page<UserVO>> pageList(@ModelAttribute UserPageDTO req) {
        Page<UserDTO> page = new Page<>(req.getPageNum(), req.getPageSize());
        LoginUser loginUser = UserContextHolder.getCurrentUser();
        log.info("start select user page, loginUser: {}, req: {}", loginUser, req);
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.USER_NOT_LOGIN);
        }
        Page<UserDTO> dtoPage = userService.selectUsersByPage(page, req);

        // 转换为 VO 对象（需要检查 UserDTO 是否包含 User 实体）
        Page<UserVO> voPage = new Page<>(dtoPage.getCurrent(), dtoPage.getSize(), dtoPage.getTotal());
        voPage.setRecords(dtoPage.getRecords().stream()
            .map(dto -> {
                User user = new User();
                user.setId(dto.getId());
                user.setTenantId(dto.getTenantId());
                user.setUsername(dto.getUsername());
                user.setEmail(dto.getEmail());
                user.setStatus(dto.getStatus());
                user.setRemark(dto.getRemark());
                user.setLastLoginAt(dto.getLastLoginAt());
                user.setCreatedAt(dto.getCreatedAt());
                user.setUpdatedAt(dto.getUpdatedAt());
                UserVO vo = UserVO.fromEntity(user);
                vo.setTenantName(dto.getTenantName());
                return vo;
            })
            .toList());

        return Result.success(voPage);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果，成功返回空字符串，失败返回错误信息
     */
    @PreAuthorize("hasAnyRole('SYS_ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @SystemLog(module = "用户管理", action = "删除用户", description = "删除用户")
    public Result<String> deleteTeamUser(@Parameter(description = "用户ID", required = true) @PathVariable String id) {
        log.info("start delete user id: {}", id);
        try {
            userService.removeOne(id);
            return Result.success();
        } catch (Exception e) {
            log.error("delete user error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 给用户分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID集合
     * @return 操作结果，成功返回空字符串，失败返回错误信息
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @PostMapping("/{userId}/roles")
    @Operation(summary = "用户分配角色")
    @SystemLog(module = "用户管理", action = "用户分配角色", description = "用户分配角色信息")
    public Result<String> assignRoles(@PathVariable String userId,
        @RequestParam(required = false) List<String> roleIds) {
        log.info("start assign roles for userId: {}, role:{}", userId, roleIds);
        userService.assignRoles(userId, roleIds);
        return Result.success();
    }

    /**
     * 获取用户角色
     *
     * @param userId 用户ID
     * @return 操作结果，包含角色ID列表 {@link List<String>}
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @GetMapping("/{userId}/roles")
    @Operation(summary = "获取用户角色")
    public Result<List<String>> getRoles(@PathVariable String userId) {
        log.info("start get roles for userId:{}", userId);
        List<UserRole> list = userRoleService.lambdaQuery()
            .eq(UserRole::getUserId, userId)
            .list();

        if (list == null || list.isEmpty()) {
            log.error("get roles error");
            return Result.success();
        }

        List<String> roleIds = list.stream()
            .map(UserRole::getRoleId)
            .toList();

        return Result.success(roleIds);
    }
}
