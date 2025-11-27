package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.exception.AppException;
import com.xiaodou.mapper.RewardItemMapper;
import com.xiaodou.mapper.RewardMapper;
import com.xiaodou.model.Reward;
import com.xiaodou.model.RewardItem;
import com.xiaodou.model.dto.reward.RewardItemImportDTO;
import com.xiaodou.model.query.RewardItemQuery;
import com.xiaodou.model.vo.RewardItemVO;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.RewardItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 奖品实例表（券码/单号库存） 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RewardItemServiceImpl extends ServiceImpl<RewardItemMapper, RewardItem> implements RewardItemService {

    private final RewardMapper rewardMapper;
    private final RewardItemMapper rewardItemMapper;

    @Override
    @Transactional
    public void importRewardItems(RewardItemImportDTO importDTO, String importerId) {
        String rewardId = importDTO.getRewardId();
        List<RewardItemImportDTO.RewardItemDetail> itemsToImport = importDTO.getItems();

        if (CollectionUtils.isEmpty(itemsToImport)) {
            throw new AppException(ResultCodeEnum.PARAM_VALID_ERROR, "导入列表不能为空");
        }

        // 1. 校验奖品是否存在
        Reward reward = rewardMapper.selectById(rewardId);
        if (reward == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "关联的奖品不存在");
        }

        // 2. 构造奖品实例实体列表
        List<RewardItem> rewardItems = itemsToImport.stream().map(detail -> {
            RewardItem item = new RewardItem();
            item.setRewardId(rewardId);
            item.setItemValue(detail.getItemValue());
            item.setTargetUserId(detail.getTargetUserId());
            item.setTargetPhoneNumber(detail.getTargetPhoneNumber());
            item.setImporterId(importerId);
            item.setStatus(RewardItem.RewardItemStatus.AVAILABLE.getCode());
            return item;
        }).collect(Collectors.toList());

        // 3. 批量保存奖品实例
        this.saveBatch(rewardItems);

        // 4. 原子性地更新奖品总库存
        int importCount = rewardItems.size();
        int updatedRows = rewardMapper.increaseTotalQuantity(rewardId, importCount);
        if (updatedRows == 0) {
            throw new AppException(ResultCodeEnum.OPERATION_FAILED, "更新奖品总库存失败，奖品可能已被删除");
        }

        log.info("成功为奖品ID [{}] 导入 {} 个实例", rewardId, importCount);
    }

    @Override
    public IPage<RewardItemVO> pageListRewardItems(RewardItemQuery query) {
        Page<RewardItemVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        // 注意：此处的 selectPageWithDetails 方法需要在 RewardItemMapper.xml 中自定义实现
        IPage<RewardItemVO> pageResult = rewardItemMapper.selectPageWithDetails(page, query);
        return pageResult;
    }
}