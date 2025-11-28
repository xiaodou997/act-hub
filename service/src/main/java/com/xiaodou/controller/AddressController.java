package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.UserAddress;
import com.xiaodou.result.Result;
import com.xiaodou.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "收货地址", description = "用户收货地址维护")
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final UserAddressService userAddressService;

    @GetMapping("/page")
    @Operation(summary = "分页查询我的地址")
    public Result<IPage<UserAddress>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = UserContextHolder.getUserId();
        IPage<UserAddress> page = userAddressService.pageByUser(new Page<>(pageNum, pageSize), userId);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取地址详情")
    public Result<UserAddress> get(@PathVariable String id) {
        UserAddress addr = userAddressService.getById(id);
        return Result.success(addr);
    }

    @PostMapping
    @Operation(summary = "新增地址")
    public Result<String> create(@Valid @RequestBody UserAddress payload) {
        payload.setUserId(UserContextHolder.getUserId());
        userAddressService.save(payload);
        return Result.success(payload.getId());
    }

    @PutMapping
    @Operation(summary = "更新地址")
    public Result<Void> update(@Valid @RequestBody UserAddress payload) {
        String userId = UserContextHolder.getUserId();
        payload.setUserId(userId);
        userAddressService.updateById(payload);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址")
    public Result<Void> delete(@PathVariable String id) {
        userAddressService.removeById(id);
        return Result.success();
    }
}

