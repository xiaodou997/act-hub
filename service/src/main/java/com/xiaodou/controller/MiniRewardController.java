package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.UserAddress;
import com.xiaodou.model.vo.RewardPayoutVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.RewardPayoutService;
import com.xiaodou.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "小程序-奖励中心", description = "我的奖励与兑换")
@RestController
@RequestMapping("/api/reward")
@RequiredArgsConstructor
public class MiniRewardController {

    private final RewardPayoutService rewardPayoutService;
    private final UserAddressService addressService;

    @GetMapping("/my")
    @Operation(summary = "分页查询我的奖励")
    public Result<IPage<RewardPayoutVO>> myRewards(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = UserContextHolder.getUserId();
        RewardPayoutQueryExt q = new RewardPayoutQueryExt();
        q.setUserId(userId);
        IPage<RewardPayoutVO> page = rewardPayoutService.pageListPayouts(q.toQuery(pageNum, pageSize));
        return Result.success(page);
    }

    @GetMapping("/{recordId}")
    @Operation(summary = "奖励详情")
    public Result<RewardPayoutVO> detail(@PathVariable String recordId) {
        // 直接沿用已有查询
        RewardPayoutVO vo = rewardPayoutService.getPayoutDetail(recordId);
        return Result.success(vo);
    }

    @PostMapping("/{recordId}/redeem")
    @Operation(summary = "兑换/填写地址")
    public Result<String> redeem(@PathVariable String recordId, @RequestBody RedeemDTO dto) {
        String userId = UserContextHolder.getUserId();
        if (dto.getAddressId() != null) {
            UserAddress addr = addressService.getById(dto.getAddressId());
            if (addr == null || !userId.equals(addr.getUserId())) {
                return Result.fail(403, "地址无效");
            }
        }
        // 此处调用 RewardPayoutService 中的发放/兑换逻辑或记录填写地址的行为
        boolean ok = rewardPayoutService.redeem(recordId, userId, dto.getAddressId());
        return ok ? Result.success("OK") : Result.fail("兑换失败");
    }

    public static class RedeemDTO {
        private String addressId;
        public String getAddressId() { return addressId; }
        public void setAddressId(String addressId) { this.addressId = addressId; }
    }

    // 适配现有 RewardPayoutService 的查询参数对象（封装）
    public static class RewardPayoutQueryExt {
        private String userId;
        public void setUserId(String userId) { this.userId = userId; }
        public com.xiaodou.model.query.RewardPayoutQuery toQuery(Integer pageNum, Integer pageSize) {
            com.xiaodou.model.query.RewardPayoutQuery q = new com.xiaodou.model.query.RewardPayoutQuery();
            q.setPageNum(pageNum);
            q.setPageSize(pageSize);
            q.setUserId(userId);
            return q;
        }
    }
}

