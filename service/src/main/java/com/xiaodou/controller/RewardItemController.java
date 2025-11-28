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
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

    @Operation(summary = "批量导入CSV", description = "上传CSV文件导入奖品实例，列为userId,phone,itemValue")
    @PostMapping("/import/csv")
    public Result<Void> importRewardItemsCsv(@RequestParam("rewardId") String rewardId,
                                             @RequestParam("file") MultipartFile file) throws Exception {
        String importerId = UserContextHolder.getUserId();
        List<RewardItemImportDTO.RewardItemDetail> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                String[] parts = trimmed.split(",");
                if (first && parts.length >= 1 && parts[0].toLowerCase().contains("userid")) {
                    first = false;
                    continue;
                }
                first = false;
                String userId = parts.length > 0 ? parts[0].trim() : null;
                String phone = parts.length > 1 ? parts[1].trim() : null;
                String itemValue = parts.length > 2 ? parts[2].trim() : null;
                RewardItemImportDTO.RewardItemDetail d = new RewardItemImportDTO.RewardItemDetail();
                d.setTargetUserId(userId);
                d.setTargetPhoneNumber(phone);
                d.setItemValue(itemValue);
                items.add(d);
            }
        }
        RewardItemImportDTO dto = new RewardItemImportDTO();
        dto.setRewardId(rewardId);
        dto.setItems(items);
        rewardItemService.importRewardItems(dto, importerId);
        return Result.success();
    }
}
