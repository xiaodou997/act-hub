package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.dto.reward.RewardPayoutDTO;
import com.xiaodou.model.query.RewardPayoutQuery;
import com.xiaodou.model.vo.RewardPayoutVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.RewardPayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 奖励发放记录表 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
@Tag(name = "奖励发放管理", description = "执行奖励发放和查询发放记录的接口")
@RestController
@RequestMapping("/reward-payout")
@RequiredArgsConstructor
public class RewardPayoutController {

    private final RewardPayoutService rewardPayoutService;

    @Operation(summary = "批量发放奖励", description = "根据任务和用户范围，执行批量发奖操作")
    @PostMapping("/distribute")
    public Result<String> distributeRewards(@Valid @RequestBody RewardPayoutDTO payoutDTO) {
        String operatorId = UserContextHolder.getUserId();
        int successCount = rewardPayoutService.distributeRewards(payoutDTO, operatorId);
        return Result.success("成功发放 " + successCount + " 个奖励");
    }

    @Operation(summary = "分页查询奖励发放记录", description = "根据查询条件分页获取奖励发放记录列表")
    @GetMapping("/page")
    public Result<IPage<RewardPayoutVO>> pageListPayouts(@Valid RewardPayoutQuery query) {
        IPage<RewardPayoutVO> page = rewardPayoutService.pageListPayouts(query);
        return Result.success(page);
    }
}
