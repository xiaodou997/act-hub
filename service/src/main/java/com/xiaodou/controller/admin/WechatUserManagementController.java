package com.xiaodou.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.model.WechatUser;
import com.xiaodou.model.dto.UpdateUserStatusDTO;
import com.xiaodou.model.dto.WechatUserUpdateDTO;
import com.xiaodou.model.query.WechatUserQuery;
import com.xiaodou.result.Result;
import com.xiaodou.service.WechatUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 微信小程序用户管理后台控制器
 */
@RestController
@RequestMapping("/admin/wechat-users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 整个控制器都需要管理员权限
public class WechatUserManagementController {

    private final WechatUserService wechatUserService;

    /**
     * 分页查询微信用户列表
     *
     * @param query 查询条件
     * @return 分页的用户数据
     */
    @GetMapping
    public Result<Page<WechatUser>> listUsers(WechatUserQuery query) {
        Page<WechatUser> pageResult = wechatUserService.listUsersByPage(query);
        return Result.success(pageResult);
    }

    /**
     * 获取单个微信用户的详细信息
     *
     * @param id 用户ID
     * @return 用户详细信息
     */
    @GetMapping("/{id}")
    public Result<WechatUser> getUserDetail(@PathVariable Long id) {
        WechatUser user = wechatUserService.getById(id);
        return Result.success(user);
    }

    /**
     * 修改微信用户的状态（启用/禁用）
     *
     * @param id      用户ID
     * @param statusDTO 包含新状态的 DTO
     * @return 操作结果
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable String id, @RequestBody UpdateUserStatusDTO statusDTO) {
        if (statusDTO.getStatus() == null || (statusDTO.getStatus() != 0 && statusDTO.getStatus() != 1)) {
            return Result.fail(400, "无效的状态值");
        }
        WechatUser user = new WechatUser();
        user.setId(id);
        user.setStatus(statusDTO.getStatus());
        boolean success = wechatUserService.updateById(user);
        return success ? Result.success() : Result.fail(500, "更新失败");
    }

    /**
     * 修改微信用户的其他信息（如昵称、手机号、备注等）
     *
     * @param id      用户ID
     * @param updateDTO 包含更新字段的 DTO
     * @return 操作结果
     */
    @PatchMapping("/{id}")
    public Result<Void> updateUserInfo(@PathVariable String id, @RequestBody WechatUserUpdateDTO updateDTO) {
        WechatUser user = new WechatUser();
        user.setId(id);
        // 仅更新 DTO 中非空的字段
        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
        }
        if (updateDTO.getGender() != null) {
            user.setGender(updateDTO.getGender());
        }
        if (updateDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDTO.getPhoneNumber());
        }
        if (updateDTO.getRemark() != null) {
            user.setRemark(updateDTO.getRemark());
        }

        boolean success = wechatUserService.updateById(user);
        return success ? Result.success() : Result.fail(500, "更新失败");
    }
}