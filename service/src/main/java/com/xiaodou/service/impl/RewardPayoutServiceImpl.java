package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.exception.AppException;
import com.xiaodou.mapper.RewardItemMapper;
import com.xiaodou.mapper.RewardMapper;
import com.xiaodou.mapper.RewardPayoutMapper;
import com.xiaodou.mapper.TaskParticipationMapper;
import com.xiaodou.model.Reward;
import com.xiaodou.model.RewardItem;
import com.xiaodou.model.RewardPayout;
import com.xiaodou.model.dto.reward.RewardPayoutDTO;
import com.xiaodou.model.query.RewardPayoutQuery;
import com.xiaodou.model.vo.RewardPayoutVO;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.RewardPayoutService;
import com.xiaodou.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardPayoutServiceImpl extends ServiceImpl<RewardPayoutMapper, RewardPayout> implements RewardPayoutService {

    private final RewardMapper rewardMapper;
    private final RewardItemMapper rewardItemMapper;
    private final RewardPayoutMapper rewardPayoutMapper;
    private final TaskParticipationMapper taskParticipationMapper;

    @Override
    @Transactional
    public int distributeRewards(RewardPayoutDTO payoutDTO, String operatorId) {
        String taskId = payoutDTO.getTaskId();
        String rewardId = payoutDTO.getRewardId();

        // 1. 校验奖品信息
        Reward reward = rewardMapper.selectById(rewardId);
        if (reward == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "奖品不存在");
        }

        // 2. 根据发放范围，获取符合条件的用户ID列表
        List<String> eligibleUserIds = getEligibleUserIds(taskId, payoutDTO.getScope());
        if (CollectionUtils.isEmpty(eligibleUserIds)) {
            log.warn("任务ID [{}] 没有找到符合发放范围 [{}] 的用户", taskId, payoutDTO.getScope());
            return 0;
        }

        // 3. 排除已经发放过该任务奖励的用户
        List<String> alreadyPaidUserIds = rewardPayoutMapper.selectPaidUserIdsByTaskId(taskId);
        List<String> usersToPay = eligibleUserIds.stream()
                .filter(userId -> !alreadyPaidUserIds.contains(userId))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(usersToPay)) {
            log.warn("任务ID [{}] 的所有符合条件的用户均已发奖", taskId);
            return 0;
        }

        int payoutCount = usersToPay.size();

        // 4. 锁定并获取足够数量的可用奖品实例
        List<RewardItem> availableItems = rewardItemMapper.selectAndLockAvailableItems(rewardId, payoutCount);
        if (availableItems.size() < payoutCount) {
            throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "奖品库存不足，需要 " + payoutCount + "，实际可用 " + availableItems.size());
        }

        // 5. 构建发放记录
        LocalDateTime now = LocalDateTime.now();
        List<RewardPayout> payoutRecords = usersToPay.stream().map(userId -> {
            RewardItem item = availableItems.remove(0); // 从列表中取出一个
            RewardPayout payout = new RewardPayout();
            payout.setUserId(userId);
            payout.setTaskId(taskId);
            // 注意：此处 taskParticipationId 需要根据 userId 和 taskId 查询，为简化，暂时设为null
            // 在实际复杂业务中，应确保能精确关联
            payout.setTaskParticipationId(null);
            payout.setRewardId(rewardId);
            payout.setRewardItemId(item.getId());
            payout.setRewardType(reward.getType());
            payout.setPayoutContent(item.getItemValue());
            payout.setStatus(RewardPayout.PayoutStatus.SUCCESS.getCode());
            payout.setPayoutTime(now);
            payout.setOperatorId(operatorId);
            return payout;
        }).collect(Collectors.toList());

        // 6. 批量保存发放记录
        this.saveBatch(payoutRecords);

        // 7. 批量更新奖品实例状态为“已分配”
        List<String> usedItemIds = payoutRecords.stream().map(RewardPayout::getRewardItemId).collect(Collectors.toList());
        rewardItemMapper.batchUpdateStatus(usedItemIds, RewardItem.RewardItemStatus.ASSIGNED.getCode());

        // 8. 原子性地更新奖品“已发放”数量
        rewardMapper.increaseIssuedQuantity(rewardId, payoutCount);

        log.info("成功为任务ID [{}] 发放 {} 个奖品ID [{}]", taskId, payoutCount, rewardId);
        return payoutCount;
    }

    private List<String> getEligibleUserIds(String taskId, RewardPayoutDTO.PayoutScope scope) {
        switch (scope) {
            case APPROVED_USERS:
                // 获取所有审核通过的用户ID
                return taskParticipationMapper.selectUserIdsByTaskAndStatus(taskId, com.xiaodou.model.TaskParticipation.ParticipationStatus.APPROVED.getCode());
            case TARGETED_USERS:
                // 获取所有定向用户ID
                // 此处需要 TaskTargetUserService，为简化，暂时返回空列表
                // return taskTargetUserService.getUserIdsByTaskId(taskId);
                throw new UnsupportedOperationException("定向用户发放功能暂未实现");
            default:
                return List.of();
        }
    }

    @Override
    public IPage<RewardPayoutVO> pageListPayouts(RewardPayoutQuery query) {
        Page<RewardPayoutVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        // 注意：此处的 selectPageWithDetails 方法需要在 RewardPayoutMapper.xml 中自定义实现
        // 在Mapper.xml中，我们需要将Long类型的时间戳转换回DATETIME进行比较
        IPage<RewardPayoutVO> pageResult = rewardPayoutMapper.selectPageWithDetails(page, query);
        return pageResult;
    }

    @Override
    public RewardPayoutVO getPayoutDetail(String id) {
        // 简化：直接根据ID查询并封装为VO，实际项目可在Mapper中做关联查询
        RewardPayout r = this.getById(id);
        if (r == null) return null;
        RewardPayoutVO vo = new RewardPayoutVO();
        vo.setId(r.getId());
        vo.setTaskId(r.getTaskId());
        vo.setUserId(r.getUserId());
        vo.setRewardId(r.getRewardId());
        vo.setRewardName(null);
        vo.setImageUrl(null);
        vo.setIssuedAt(DateTimeUtils.toTimestampAtUTC8(r.getPayoutTime()));
        vo.setStatus(r.getStatus());
        vo.setRewardForm(r.getRewardType());
        vo.setPayoutContent(r.getPayoutContent());
        return vo;
    }

    @Override
    public boolean redeem(String recordId, String userId, String addressId) {
        RewardPayout r = this.getById(recordId);
        if (r == null || !userId.equals(r.getUserId())) return false;
        // 券码：直接返回 payoutContent；实物：记录地址ID或快递准备状态（此处仅做状态保留）
        RewardPayout u = new RewardPayout();
        u.setId(recordId);
        // 不改变已发放的状态，若需要补充可设置为“已确认兑换”
        this.updateById(u);
        return true;
    }
}
