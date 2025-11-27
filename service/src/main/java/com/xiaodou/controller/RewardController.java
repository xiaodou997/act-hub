package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.dto.reward.RewardCreateDTO;
import com.xiaodou.model.dto.reward.RewardUpdateDTO;
import com.xiaodou.model.query.RewardQuery;
import com.xiaodou.model.vo.RewardVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 奖品配置表 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
@Tag(name = "奖品管理", description = "奖品配置的增删改查接口")
@RestController
@RequestMapping("/reward")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @Operation(summary = "创建奖品", description = "创建一个新的奖品配置")
    @PostMapping
    public Result<String> createReward(@Valid @RequestBody RewardCreateDTO createDTO) {
        String creatorId = UserContextHolder.getUserId(); // 从上下文中获取当前操作用户ID
        String rewardId = rewardService.createReward(createDTO, creatorId);
        return Result.success(rewardId);
    }

    @Operation(summary = "更新奖品", description = "更新一个已有的奖品配置")
    @PutMapping
    public Result<Boolean> updateReward(@Valid @RequestBody RewardUpdateDTO updateDTO) {
        boolean success = rewardService.updateReward(updateDTO);
        return Result.success(success);
    }

    @Operation(summary = "删除奖品（归档）", description = "逻辑删除奖品，将其状态设置为归档")
    @Parameter(name = "id", description = "奖品ID", required = true)
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteReward(@PathVariable String id) {
        boolean success = rewardService.deleteReward(id);
        return Result.success(success);
    }

    @Operation(summary = "分页查询奖品列表", description = "根据查询条件分页获取奖品列表")
    @GetMapping("/page")
    public Result<IPage<RewardVO>> pageListRewards(@Valid RewardQuery query) {
        IPage<RewardVO> page = rewardService.pageListRewards(query);
        return Result.success(page);
    }

    @Operation(summary = "获取奖品详情", description = "根据ID获取单个奖品的详细信息")
    @Parameter(name = "id", description = "奖品ID", required = true)
    @GetMapping("/{id}")
    public Result<RewardVO> getRewardDetail(@PathVariable String id) {
        RewardVO rewardVO = rewardService.getRewardDetail(id);
        return Result.success(rewardVO);
    }
}
