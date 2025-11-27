package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.dto.reward.RewardItemImportDTO;
import com.xiaodou.model.query.RewardItemQuery;
import com.xiaodou.model.vo.RewardItemVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.RewardItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 奖品实例表（券码/单号库存） 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
@Tag(name = "奖品实例管理", description = "奖品券码/单号的库存管理接口")
@RestController
@RequestMapping("/reward-item")
@RequiredArgsConstructor
public class RewardItemController {

    private final RewardItemService rewardItemService;

    @Operation(summary = "批量导入奖品实例", description = "批量导入券码或为实物奖品创建库存条目")
    @PostMapping("/import")
    public Result<Void> importRewardItems(@Valid @RequestBody RewardItemImportDTO importDTO) {
        String importerId = UserContextHolder.getUserId();
        rewardItemService.importRewardItems(importDTO, importerId);
        return Result.success();
    }

    @Operation(summary = "分页查询奖品实例列表", description = "根据查询条件分页获取奖品实例列表")
    @GetMapping("/page")
    public Result<IPage<RewardItemVO>> pageListRewardItems(@Valid RewardItemQuery query) {
        IPage<RewardItemVO> page = rewardItemService.pageListRewardItems(query);
        return Result.success(page);
    }
}
